package org.sagemath.droid.fragments;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Pair;
import android.webkit.CookieSyncManager;
import com.google.gson.Gson;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.otto.Subscribe;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.message.BasicNameValuePair;
import org.sagemath.droid.constants.ExecutionState;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.events.InteractUpdateEvent;
import org.sagemath.droid.events.ProgressEvent;
import org.sagemath.droid.events.ServerDisconnectEvent;
import org.sagemath.droid.events.ShareAvailableEvent;
import org.sagemath.droid.models.gson.*;
import org.sagemath.droid.utils.BusProvider;
import org.sagemath.droid.utils.CookieManagerProvider;
import org.sagemath.droid.utils.UrlUtils;
import org.sagemath.droid.websocket.WebSocketClient;

import java.io.IOException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.sagemath.droid.events.ServerDisconnectEvent.DisconnectType.*;

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

    private OkHttpClient httpClient;
    private CookieManager cookieManager;
    private Gson gson;
    private SageAsyncTask asyncTask;

    private WebSocketClient shellClient, ioPubClient;

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
        httpClient = new OkHttpClient();
        cookieManager = CookieManagerProvider.getInstance();
        httpClient.setCookieHandler(cookieManager);
        gson = new Gson();
    }

    public void closeWebSockets() {
        if (shellClient != null && ioPubClient != null) {
            shellClient.disconnect();
            ioPubClient.disconnect();
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

        String url = UrlUtils.getPermalinkURL();

        RequestBody formBody = new FormEncodingBuilder()
                .add(VALUE_CODE, request.getContent().getCode())
                .build();

        com.squareup.okhttp.Request permalinkRequest = new com.squareup.okhttp.Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Response response = httpClient.newCall(permalinkRequest).execute();

        if (!response.isSuccessful())
            BusProvider.getInstance().post(new ServerDisconnectEvent(DISCONNECT_HTTP_ERROR));

        permalinkResponse = gson.fromJson(response.body().charStream(), PermalinkResponse.class);

        Log.i(TAG, "Permalink Response" + gson.toJson(permalinkResponse));

        return permalinkResponse;
    }

    private WebSocketResponse sendInitialRequest() throws IOException {

        WebSocketResponse webSocketResponse;

        String url = UrlUtils.getInitialKernelURL();

        RequestBody formBody = new FormEncodingBuilder()
                .add(HEADER_ACCEPT_ENCODING, VALUE_IDENTITY)
                .add(HEADER_TOS, "true")
                .build();

        com.squareup.okhttp.Request initialRequest = new com.squareup.okhttp.Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Response response = httpClient.newCall(initialRequest).execute();

        if(!response.isSuccessful())
            BusProvider.getInstance().post(new ServerDisconnectEvent(DISCONNECT_HTTP_ERROR));

        webSocketResponse = gson.fromJson(response.body().charStream(), WebSocketResponse.class);

        Log.i(TAG, "Received Websocket Response: " + gson.toJson(webSocketResponse));

        Log.i(TAG, "Cookies" + Arrays.asList(cookieManager.getCookieStore().getCookies()));

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

    @SuppressWarnings("unused")
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
            shellClient.send(gson.toJson(currentExecuteRequest));
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
            , WebSocketClient.Listener shellCallback
            , WebSocketClient.Listener ioPubCallback) {

        List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
        ArrayList<BasicNameValuePair> headers = new ArrayList<>();
        StringBuilder builder = new StringBuilder();

        //Add the cookies to websocket request

        CookieSyncManager.createInstance(getActivity());
        android.webkit.CookieManager manager = android.webkit.CookieManager.getInstance();

        manager.setAcceptCookie(true);
        manager.removeSessionCookie();
        CookieSyncManager.getInstance().sync();

        for (int i = 0; i < cookies.size(); i++) {

            //Add the same cookies to WebKit so WebViews can use it
            manager.setCookie(cookies.get(i).getDomain(),cookies.get(i).getName() + "=" + cookies.get(i).getValue());

            if (i < cookies.size() - 1)
                builder.append(cookies.get(i).getName() + "=" + cookies.get(i).getValue() + ";");
            else
                builder.append(cookies.get(i).getName() + "=" + cookies.get(i).getValue());
        }

        //Sync the Cookies for WebViews
        CookieSyncManager.getInstance().sync();

        Log.i(TAG, "Adding Cookies: " + builder.toString());
        headers.add(new BasicNameValuePair("Cookie", builder.toString()));

        shellClient = new WebSocketClient(URI.create(shellURL), shellCallback, headers);
        ioPubClient = new WebSocketClient(URI.create(ioPubURL), ioPubCallback, headers);

        shellClient.connect();
        ioPubClient.connect();
    }

    private WebSocketClient.Listener shellCallback = new WebSocketClient.Listener() {
        @Override
        public void onConnect() {
            Log.i(TAG, "Shell Connected, Sending " + queryCode);
            shellClient.send(queryCode);
        }

        @Override
        public void onMessage(String message) {
            Log.i(TAG, "Shell Received Message" + message);
        }

        @Override
        public void onMessage(byte[] data) {

        }

        @Override
        public void onDisconnect(int code, String reason) {
            Log.i(TAG, "Shell Closed" + reason);
        }

        @Override
        public void onError(Exception error) {
            Log.i(TAG, "Shell Error: " + error.getMessage());
        }
    };

    private WebSocketClient.Listener ioPubCallback = new WebSocketClient.Listener() {
        @Override
        public void onConnect() {

        }

        @Override
        public void onMessage(String message) {
            try {
                Log.i(TAG, "Got Shell Message: " + message);
                final BaseReply reply = BaseReply.parse(message);
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


        @Override
        public void onMessage(byte[] data) {

        }

        @Override
        public void onDisconnect(int code, String reason) {

            //If Activity does not exist, i.e Fragment is detached, early breakout
            if(getActivity()==null)
                return;

            if (isInteractInput) {
                Log.i(TAG, "Executing Disconnect Callback");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BusProvider.getInstance().post(new ServerDisconnectEvent(DISCONNECT_INTERACT));
                    }
                });
            }

        }

        @Override
        public void onError(Exception error) {
            Log.i(TAG, "IOPub Error:" + error);
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
            } catch (ConnectTimeoutException timeoutException) {
                BusProvider.getInstance().post(new ServerDisconnectEvent(DISCONNECT_TIMEOUT));
            } catch (Exception e) {
                Log.e(TAG, "Could not send request: " + e.getMessage());
            }
            return null;

        }

        @Override
        protected void onPostExecute(Pair<BaseResponse, BaseResponse> responses) {
            parseResponses(responses);
        }
    }
}
