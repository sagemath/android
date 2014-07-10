package org.sagemath.droid;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.squareup.otto.Subscribe;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.sagemath.droid.constants.ExecutionState;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.events.ServerDisconnectEvent;
import org.sagemath.droid.events.InteractUpdateEvent;
import org.sagemath.droid.events.ProgressEvent;
import org.sagemath.droid.models.gson.*;
import org.sagemath.droid.utils.BusProvider;
import org.sagemath.droid.utils.UrlUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

/**
 * @author Haven
 */
public class SageSingleCell {

    private static final String TAG = "SageDroid:SageSingleCell2";

    private static final String HEADER_ACCEPT_ENCODING = "Accept_Encoding";
    private static final String HEADER_TOS = "accepted_tos";
    private static final String VALUE_IDENTITY = "identity";
    private static final String VALUE_CODE = "code";

    private String permalinkURL;
    private String initialRequestString;
    private String kernelID;
    private boolean isInteractInput;

    private Context context;

    private Request executeRequest, currentExecuteRequest;
    private WebSocket shellSocket;

    private PostTask postTask;
    private ShareTask shareTask;

    private Gson gson;
    private DefaultHttpClient httpClient;

    //--- INTERFACE RELATED ---
    public interface OnSageListener {

        public void onSageReplyListener(BaseReply reply);

        public void onSageAdditionalReplyListener(BaseReply reply);

        public void onSageInteractListener(InteractReply reply);

        public void onSageFinishedListener(BaseReply reason);

        public void onInteractUpdated();
    }

    private OnSageListener onSageListener;

    public void setOnSageListener(OnSageListener onSageListener) {
        this.onSageListener = onSageListener;
    }

    //---CLASS METHODS---

    public SageSingleCell(Context context) {
        this.context = context;
        isInteractInput = false;
        BusProvider.getInstance().register(this);
        gson = new Gson();

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpClient = new DefaultHttpClient(params);
    }

    public void query(String sageInput) {
        //Initialize a new ExecuteRequest object
        currentExecuteRequest = executeRequest = new Request(sageInput);

        Log.i(TAG, "Creating new ExecuteRequest: " + gson.toJson(executeRequest));

        shareTask = new ShareTask();
        shareTask.execute(executeRequest);

        postTask = new PostTask();
        postTask.execute(executeRequest);
    }

    public void cancelTasks() {
        //TODO way to actually cancel AsyncTask here, which would involve some sort of loop to check if computation is finished
    }

    public void addReply(BaseReply reply) {

        Log.i(TAG, "Adding Reply:" + reply.getStringMessageType());

        if (reply instanceof ImageReply) {
            ImageReply imageReply = (ImageReply) reply;
            imageReply.setKernelID(kernelID);
            if (reply.isReplyTo(currentExecuteRequest))
                onSageListener.onSageAdditionalReplyListener(imageReply);
            else
                onSageListener.onSageReplyListener(imageReply);

        } else if (reply instanceof InteractReply) {
            Log.i(TAG, "Reply is Interact, calling onSageInteractListener");
            isInteractInput = true;
            InteractReply interactReply = (InteractReply) reply;
            onSageListener.onSageInteractListener(interactReply);
        } else if (reply instanceof StatusReply) {
            //If the reply is a status with idle/dead execution state, a computation
            //has finished or terminated, inform the SageActivity
            StatusReply statusReply = (StatusReply) reply;
            if (statusReply.getContent().getExecutionState() == ExecutionState.IDLE
                    || statusReply.getContent().getExecutionState() == ExecutionState.DEAD) {
                onSageListener.onSageReplyListener(reply);
                onSageListener.onSageFinishedListener(reply);
            }
        } else if (reply instanceof PythonInputReply) {
            if (((PythonInputReply) reply).isInteractUpdateReply()) {
                onSageListener.onInteractUpdated();
            }
            return;
        } else if (reply.isReplyTo(currentExecuteRequest)) {
            Log.i(TAG, "Reply to current execute request");
            onSageListener.onSageAdditionalReplyListener(reply);
        } else {
            Log.i(TAG, "Reply is output");
            onSageListener.onSageReplyListener(reply);
        }
    }

    public WebSocketResponse sendInitialRequest() throws IOException {

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

        kernelID = webSocketResponse.getKernelID();

        return webSocketResponse;

    }

    public PermalinkResponse sendPermalinkRequest(Request request) throws IOException {

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

    public void setupWebSockets(String shellURL, String ioPubURL
            , AsyncHttpClient.WebSocketConnectCallback shellCallback
            , AsyncHttpClient.WebSocketConnectCallback ioPubCallback) {

        AsyncHttpClient.getDefaultInstance().websocket(shellURL, "ws", shellCallback);
        AsyncHttpClient.getDefaultInstance().websocket(ioPubURL, "ws", ioPubCallback);
    }

    private class PostTask extends AsyncTask<Request, Void, Void> {

        @Override
        protected void onPreExecute() {
            //progressIntent.putExtra(StringConstants.ARG_PROGRESS_START, true);
            //localBroadcastManager.sendBroadcast(progressIntent);
            BusProvider.getInstance().post(new ProgressEvent(StringConstants.ARG_PROGRESS_START));
        }

        @Override
        protected Void doInBackground(Request... requests) {
            initialRequestString = gson.toJson(requests[0]);

            Log.i(TAG, "Request String:" + initialRequestString);

            try {
                //initialRequestString= testRequest.toJSON().toString();
                WebSocketResponse response = sendInitialRequest();

                if (response.isValidResponse()) {
                    Log.i(TAG, "Response is valid,Setting up Websockets");
                    String shellURL = UrlUtils.getShellURL(response.getKernelID(), response.getWebSocketURL());
                    String ioPubURL = UrlUtils.getIoPubURL(response.getKernelID(), response.getWebSocketURL());

                    setupWebSockets(shellURL, ioPubURL, shellCallback, ioPubCallback);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //BusProvider.getInstance().post(new ProgressEvent(StringConstants.ARG_PROGRESS_END));
        }
    }

    private class ShareTask extends AsyncTask<Request, Void, Void> {

        @Override
        protected void onPreExecute() {
            BusProvider.getInstance().post(new ProgressEvent(StringConstants.ARG_PROGRESS_START));
        }

        @Override
        protected Void doInBackground(Request... requests) {

            try {
                PermalinkResponse response = sendPermalinkRequest(requests[0]);
                permalinkURL = response.getQueryURL();
                Log.i(TAG, "Permalink URL:" + permalinkURL);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            //BusProvider.getInstance().post(StringConstants.ARG_PROGRESS_END);
        }
    }

    public URI getShareURI() {
        URI shareURI = URI.create(permalinkURL);

        if (shareURI != null)
            return shareURI;

        //TODO Error Handler msg here
        return null;
    }

    public String formatInteractUpdate(String interactID, String name, String value) {

        String template = "sys._sage_.update_interact(\"%s\",\"%s\",%s)";

        return String.format(template, interactID, name, value);

    }

    @Subscribe
    public void onInteractUpdate(InteractUpdateEvent event) {
        Log.i(TAG, "UPDATING INTERACT VARIABLE: " + event.getVarName());
        Log.i(TAG, "UPDATED INTERACT VALUE: " + event.getValue().toString());

        String interactID = event.getReply().getContent().getData().getInteract().getNewInteractID();
        String sageInput = formatInteractUpdate(interactID, event.getVarName(), event.getValue().toString());
        Log.i(TAG, "Updating Interact: " + sageInput);

        currentExecuteRequest = executeRequest = new Request(sageInput, event.getReply().getHeader().getSession());

        Log.i(TAG, "Sending Interact Update Request:" + gson.toJson(executeRequest));
        shellSocket.send(gson.toJson(executeRequest));
    }

    public void updateInteract(InteractReply interact, String varName, Object newValue) {
        Log.i(TAG, "UPDATING INTERACT VARIABLE: " + varName);
        Log.i(TAG, "UPDATED INTERACT VALUE: " + newValue);

        String interactID = interact.getContent().getData().getInteract().getNewInteractID();
        String sageInput = formatInteractUpdate(interactID, varName, newValue.toString());
        Log.i(TAG, "Updating Interact: " + sageInput);

        currentExecuteRequest = executeRequest = new Request(sageInput, interact.getHeader().getSession());

        Log.i(TAG, "Sending Interact Update Request:" + gson.toJson(executeRequest));
        shellSocket.send(gson.toJson(executeRequest));
    }

    private AsyncHttpClient.WebSocketConnectCallback shellCallback = new AsyncHttpClient.WebSocketConnectCallback() {
        @Override
        public void onCompleted(Exception e, WebSocket webSocket) {
            //Send the execute_request
            if (e != null) {
                Log.i(TAG, e.getMessage());
            }
            shellSocket = webSocket;
            Log.i(TAG, "Shell Connected, Sending " + initialRequestString);
            shellSocket.send(initialRequestString);

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
            webSocket.setStringCallback(new WebSocket.StringCallback() {
                @Override
                public void onStringAvailable(String s) {
                    Log.i(TAG, "IOPub received String" + s);
                    try {
                        BaseReply reply = BaseReply.parse(s);
                        addReply(reply);
                    } catch (Exception e) {
                        Log.i(TAG, e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
            webSocket.setClosedCallback(new CompletedCallback() {
                @Override
                public void onCompleted(Exception e) {
                    if (e != null)
                        Log.i(TAG, "IOPub Closed due to:" + e.getMessage());
                    else
                        Log.i(TAG, "IOPub Closed ");

                    //If input is interactive, tell the user when the websocket disconnects.
                    if (isInteractInput) {
                        Log.i(TAG, "Executing Disconnect Callback");
                        ((Activity)context).runOnUiThread(new Runnable() {
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


}
