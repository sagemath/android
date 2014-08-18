package org.sagemath.droid.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import com.google.gson.Gson;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.squareup.otto.Subscribe;
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
import org.sagemath.droid.constants.ExecutionState;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.events.InteractUpdateEvent;
import org.sagemath.droid.events.ProgressEvent;
import org.sagemath.droid.events.ServerDisconnectEvent;
import org.sagemath.droid.events.ShareAvailableEvent;
import org.sagemath.droid.models.gson.*;
import org.sagemath.droid.utils.BusProvider;
import org.sagemath.droid.utils.UrlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

/**
 * <p>The Headless Fragment which performs all computations</p>
 *
 * @author Nikhil Peter Raj
 */
public class AsyncTaskFragment extends Fragment {

    private static final String TAG = "SageDroid:AsyncTaskFragment";

    public static interface ServerCallbacks {
        public void onReply(BaseReply reply);

        public void onComputationFinished();
    }

    private ServerCallbacks callBacks;

    private static final String HEADER_ACCEPT_ENCODING = "Accept_Encoding";
    private static final String HEADER_TOS = "accepted_tos";
    private static final String VALUE_IDENTITY = "identity";
    private static final String VALUE_CODE = "code";

    private HttpClient httpClient;
    private Gson gson;
    private SageAsyncTask asyncTask;

    private WebSocket shellSocket, ioPubSocket;

    private String queryCode;
    private String kernelID;
    private String permalinkURL;

    private boolean isInteractInput = false;

    Request currentExecuteRequest;

    public static AsyncTaskFragment getInstance() {
        return new AsyncTaskFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        init();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BusProvider.getInstance().register(this);
        callBacks = (ServerCallbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
        callBacks = null;
    }

    public void query(String sageInput) {
        //Initialize a new ExecuteRequest object
        currentExecuteRequest = new Request(sageInput);
        Log.i(TAG, "Creating new ExecuteRequest: " + gson.toJson(currentExecuteRequest));
        queryCode = gson.toJson(currentExecuteRequest);
        asyncTask = new SageAsyncTask();
        asyncTask.execute(currentExecuteRequest);

    }

    private void init() {
        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpClient = new DefaultHttpClient(params);
        gson = new Gson();
    }

    public void closeWebSockets() {
        if (shellSocket != null && ioPubSocket != null) {
            shellSocket.close();
            ioPubSocket.close();
        }
        Log.i(TAG, "Sockets closed");
    }

    public void cancel() {
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        closeWebSockets();
    }

    public URI getShareURI() {
        URI shareURI = URI.create(permalinkURL);

        if (shareURI != null)
            return shareURI;

        //TODO Error Handler msg here
        return null;
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

    public void addReply(BaseReply reply) {

        Log.i(TAG, "Adding Reply:" + reply.getStringMessageType());

        if (reply instanceof ImageReply) {
            ImageReply imageReply = (ImageReply) reply;
            imageReply.setKernelID(kernelID);
            if (callBacks != null)
                callBacks.onReply(imageReply);
        } else if (reply instanceof InteractReply) {
            Log.i(TAG, "Reply is Interact, calling onSageInteractListener");
            isInteractInput = true;
            if (callBacks != null)
                callBacks.onReply(reply);
        } else if (reply instanceof StatusReply) {
            //If the reply is a status with idle/dead execution state, a computation
            //has finished or terminated, inform the SageActivity
            StatusReply statusReply = (StatusReply) reply;
            if (statusReply.getContent().getExecutionState() == ExecutionState.IDLE
                    || statusReply.getContent().getExecutionState() == ExecutionState.DEAD) {
                if (callBacks != null) {
                    callBacks.onReply(reply);
                    callBacks.onComputationFinished();
                }
            }
        } else if (reply instanceof PythonInputReply) {
            if (((PythonInputReply) reply).isInteractUpdateReply()) {
                BusProvider.getInstance().post(new InteractUpdateEvent(null, null, null));
            }
        } else {
            Log.i(TAG, "Reply to current execute request");
            callBacks.onReply(reply);
        }
    }

    public String formatInteractUpdate(String interactID, String name, String value) {
        String template = "sys._sage_.update_interact(\"%s\",\"%s\",%s)";
        return String.format(template, interactID, name, value);
    }

    @Subscribe
    public void onInteractUpdate(InteractUpdateEvent event) {
        if (event.getReply() != null) {
            Log.i(TAG, "UPDATING INTERACT VARIABLE: " + event.getVarName());
            Log.i(TAG, "UPDATED INTERACT VALUE: " + event.getValue().toString());

            String interactID = event.getReply().getContent().getData().getInteract().getNewInteractID();
            String sageInput = formatInteractUpdate(interactID, event.getVarName(), event.getValue().toString());
            Log.i(TAG, "Updating Interact: " + sageInput);

            currentExecuteRequest = new Request(sageInput, event.getReply().getHeader().getSession());

            Log.i(TAG, "Sending Interact Update Request:" + gson.toJson(currentExecuteRequest));
            shellSocket.send(gson.toJson(currentExecuteRequest));
        }
    }

    private void parseResponses(Pair<BaseResponse, BaseResponse> responses) {
        BaseResponse firstResponse = responses.first;
        BaseResponse secondResponse = responses.second;
        if (firstResponse instanceof PermalinkResponse) {
            permalinkURL = ((PermalinkResponse) firstResponse).getQueryURL();
            BusProvider.getInstance().post(new ShareAvailableEvent(permalinkURL));
        }
        if (secondResponse instanceof WebSocketResponse) {
            if (((WebSocketResponse) secondResponse).isValidResponse()) {
                Log.d(TAG, "Response is valid");
                //Cache to avoid huge lengths
                WebSocketResponse response = (WebSocketResponse) secondResponse;
                kernelID = response.getKernelID();
                String shellURL, ioPubURL;

                shellURL = UrlUtils.getShellURL(response.getKernelID(), response.getWebSocketURL());
                ioPubURL = UrlUtils.getIoPubURL(response.getKernelID(), response.getWebSocketURL());
                setupWebSockets(shellURL, ioPubURL, shellCallback, ioPubCallback);
            }
        }
    }

    private void setupWebSockets(String shellURL, String ioPubURL
            , AsyncHttpClient.WebSocketConnectCallback shellCallback
            , AsyncHttpClient.WebSocketConnectCallback ioPubCallback) {

        AsyncHttpClient.getDefaultInstance().websocket(shellURL, "ws", shellCallback);
        AsyncHttpClient.getDefaultInstance().websocket(ioPubURL, "ws", ioPubCallback);
    }

    private AsyncHttpClient.WebSocketConnectCallback shellCallback = new AsyncHttpClient.WebSocketConnectCallback() {
        @Override
        public void onCompleted(Exception e, WebSocket webSocket) {
            //Send the execute_request
            if (e != null) {
                Log.i(TAG, e.getMessage());
            }
            shellSocket = webSocket;
            Log.i(TAG, "Shell Connected, Sending " + queryCode);
            shellSocket.send(queryCode);

            shellSocket.setStringCallback(new WebSocket.StringCallback() {
                @Override
                public void onStringAvailable(String s) {
                    Log.i(TAG, "Shell Received Message" + s);
                }
            });

            shellSocket.setClosedCallback(new CompletedCallback() {
                @Override
                public void onCompleted(Exception e) {
                    if (e != null)
                        Log.i(TAG, "Shell Closed due to: " + e.getMessage());
                    else
                        Log.i(TAG, "Shell Closed");
                }
            });
        }
    };

    private AsyncHttpClient.WebSocketConnectCallback ioPubCallback = new AsyncHttpClient.WebSocketConnectCallback() {
        @Override
        public void onCompleted(Exception e, WebSocket webSocket) {
            if (e != null) {
                Log.i(TAG, e.getMessage());
            }
            Log.i(TAG, "IOPub Connected");

            ioPubSocket = webSocket;
            ioPubSocket.setStringCallback(new WebSocket.StringCallback() {
                @Override
                public void onStringAvailable(String s) {
                    try {
                        final BaseReply reply = BaseReply.parse(s);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (reply != null)
                                    addReply(reply);
                            }
                        });

                    } catch (Exception e) {
                        Log.i(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            ioPubSocket.setClosedCallback(new CompletedCallback() {
                @Override
                public void onCompleted(Exception e) {
                    if (e != null)
                        Log.i(TAG, "IOPub Closed due to:" + e.getMessage());
                    else
                        Log.i(TAG, "IOPub Closed ");

                    //If input is interactive, tell the user when the websocket disconnects.
                    if (isInteractInput) {
                        Log.i(TAG, "Executing Disconnect Callback");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                BusProvider.getInstance().post(new ServerDisconnectEvent(true));
                            }
                        });
                        Log.i(TAG, "Disconnect posted");
                    }
                }
            });
        }
    };

    private class SageAsyncTask extends AsyncTask<Request, Void, Pair<BaseResponse, BaseResponse>> {

        @Override
        protected void onPreExecute() {
            BusProvider.getInstance().post(new ProgressEvent(StringConstants.ARG_PROGRESS_START));
        }

        @Override
        protected Pair<BaseResponse, BaseResponse> doInBackground(Request... params) {

            try {
                BaseResponse permalinkResponse, webSocketResponse;
                permalinkResponse = sendPermalinkRequest(params[0]);
                webSocketResponse = sendInitialRequest();

                return Pair.create(permalinkResponse, webSocketResponse);
            } catch (Exception e) {
                Log.e(TAG, "Error in sending request");
            }
            return null;

        }

        @Override
        protected void onPostExecute(Pair<BaseResponse, BaseResponse> responses) {
            parseResponses(responses);
        }

    }
}
