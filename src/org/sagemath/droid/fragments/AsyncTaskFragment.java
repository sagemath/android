package org.sagemath.droid.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.sagemath.droid.models.gson.BaseResponse;
import org.sagemath.droid.models.gson.PermalinkResponse;
import org.sagemath.droid.models.gson.Request;
import org.sagemath.droid.models.gson.WebSocketResponse;
import org.sagemath.droid.utils.UrlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

/**
 * Created by Haven on 11-07-2014.
 */
public class AsyncTaskFragment extends Fragment {

    private static final String TAG = "SageDroid:AsyncTaskFragment";

    private static final String HEADER_ACCEPT_ENCODING = "Accept_Encoding";
    private static final String HEADER_TOS = "accepted_tos";
    private static final String VALUE_IDENTITY = "identity";
    private static final String VALUE_CODE = "code";

    private static final String ARG_REQUEST = "request";

    private Request request;
    private HttpClient httpClient;
    private Gson gson;
    SageAsyncTask asyncTask;

    public static interface CallBacks {
        public void onPreExecute();

        public void onPostExecute(Pair<BaseResponse, BaseResponse> responses);

        public void onCancelled();
    }

    private CallBacks callBacks;

    public static AsyncTaskFragment getInstance(Request request) {

        AsyncTaskFragment fragment = new AsyncTaskFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_REQUEST, request);

        fragment.setArguments(args);

        return fragment;
    }

    public void excecuteRequest(Request request) {
        this.request = request;
        asyncTask = new SageAsyncTask();
        asyncTask.execute(request);
    }

    private void init() {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpClient = new DefaultHttpClient(params);
        gson = new Gson();
    }

    public void cancel() {
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
    }

    private PermalinkResponse sendPermalinkRequest(Request request) throws IOException {

        PermalinkResponse permalinkResponse;

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpClient = new DefaultHttpClient(params);

        String url = UrlUtils.getPermalinkURL();

        HttpPost permalinkPost = new HttpPost();
        permalinkPost.setURI(URI.create(url));
        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair(VALUE_CODE, request.getContent().getCode()));
        permalinkPost.setEntity(new UrlEncodedFormEntity(postParameters));

        Log.i(TAG, "Permalink URL: " + url);
        Log.i(TAG, "Permalink code: " + request.getContent().getCode());

        HttpResponse httpResponse = httpClient.execute(permalinkPost);
        InputStream inputStream = httpResponse.getEntity().getContent();

        permalinkResponse = gson.fromJson(new InputStreamReader(inputStream), PermalinkResponse.class);

        Log.i(TAG, "Permalink Response" + gson.toJson(permalinkResponse));

        inputStream.close();

        return permalinkResponse;
    }

    private WebSocketResponse sendInitialRequest() throws IOException {

        WebSocketResponse webSocketResponse;

        HttpPost httpPost = new HttpPost();

        String url = UrlUtils.getInitialKernelURL();

        httpPost.setURI(URI.create(url));

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
        postParameters.add(new BasicNameValuePair(HEADER_ACCEPT_ENCODING, VALUE_IDENTITY));
        postParameters.add(new BasicNameValuePair(HEADER_TOS, "true"));
        httpPost.setEntity(new UrlEncodedFormEntity(postParameters));

        HttpResponse httpResponse = httpClient.execute(httpPost);
        InputStream inputStream = httpResponse.getEntity().getContent();

        webSocketResponse = gson.fromJson(new InputStreamReader(inputStream), WebSocketResponse.class);
        inputStream.close();

        Log.i(TAG, "Received Websocket Response: " + gson.toJson(webSocketResponse));

        return webSocketResponse;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        request = getArguments().getParcelable(ARG_REQUEST);
        setRetainInstance(true);
        init();
        asyncTask = new SageAsyncTask();
        asyncTask.execute(request);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callBacks = (CallBacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callBacks = null;
    }

    private class SageAsyncTask extends AsyncTask<Request, Void, Pair<BaseResponse, BaseResponse>> {

        @Override
        protected void onPreExecute() {
            if (callBacks != null)
                callBacks.onPreExecute();
        }

        @Override
        protected Pair<BaseResponse, BaseResponse> doInBackground(Request... params) {

            try {
                BaseResponse permalinkResponse, webSocketResponse;
                permalinkResponse = sendPermalinkRequest(request);
                webSocketResponse = sendInitialRequest();

                return Pair.create(permalinkResponse, webSocketResponse);
            } catch (Exception e) {
                Log.e(TAG, "Error in sending request");
            }
            return null;

        }

        @Override
        protected void onPostExecute(Pair<BaseResponse, BaseResponse> responses) {
            if (callBacks != null)
                callBacks.onPostExecute(responses);
        }

        @Override
        protected void onCancelled() {
            if (callBacks != null)
                callBacks.onCancelled();
        }
    }
}
