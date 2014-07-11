package org.sagemath.droid;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.Pair;
import com.google.gson.Gson;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.squareup.otto.Subscribe;
import org.apache.http.HttpVersion;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.sagemath.droid.constants.ExecutionState;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.events.InteractUpdateEvent;
import org.sagemath.droid.events.ProgressEvent;
import org.sagemath.droid.events.ServerDisconnectEvent;
import org.sagemath.droid.fragments.AsyncTaskFragment;
import org.sagemath.droid.models.gson.*;
import org.sagemath.droid.utils.BusProvider;
import org.sagemath.droid.utils.UrlUtils;

import java.net.URI;

/**
 * @author Haven
 */
public class SageSingleCell {

    private static final String TAG = "SageDroid:SageSingleCell2";

    private static final String TASK_FRAGMENT_ID = "taskFragment";

    private String permalinkURL;
    private String queryCode;
    private String kernelID;
    private boolean isInteractInput;

    private Context context;
    private FragmentManager manager;
    private AsyncTaskFragment taskFragment;

    private Request executeRequest, currentExecuteRequest;
    private WebSocket shellSocket, ioPubSocket;

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

    public SageSingleCell(Context context, FragmentManager manager) {
        this.context = context;
        isInteractInput = false;
        BusProvider.getInstance().register(this);
        gson = new Gson();

        this.manager = manager;

        HttpParams params = new BasicHttpParams();
        params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
        httpClient = new DefaultHttpClient(params);
    }

    public void query(String sageInput) {
        //Initialize a new ExecuteRequest object
        currentExecuteRequest = executeRequest = new Request(sageInput);

        Log.i(TAG, "Creating new ExecuteRequest: " + gson.toJson(executeRequest));

        queryCode = gson.toJson(executeRequest);

        taskFragment = (AsyncTaskFragment) manager.findFragmentByTag(TASK_FRAGMENT_ID);

        if (taskFragment == null) {
            taskFragment = AsyncTaskFragment.getInstance(executeRequest);
            manager.beginTransaction().add(taskFragment, TASK_FRAGMENT_ID).commit();
        } else {
            taskFragment.excecuteRequest(executeRequest);
        }

    }


    public void sendProgressStart() {
        BusProvider.getInstance().post(new ProgressEvent(StringConstants.ARG_PROGRESS_START));
    }


    public void parseResponses(Pair<BaseResponse, BaseResponse> responses) {
        BaseResponse firstResponse = responses.first;
        BaseResponse secondResponse = responses.second;
        if (firstResponse instanceof PermalinkResponse) {
            permalinkURL = ((PermalinkResponse) firstResponse).getQueryURL();
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


    public void cancelComputation() {
        BusProvider.getInstance().post(new ProgressEvent(StringConstants.ARG_PROGRESS_END));
    }

    public void cancelTasks() {
        if (taskFragment != null) {
            Log.d(TAG, "Removing fragment");
            taskFragment.cancel();
            manager.beginTransaction().remove(taskFragment).commit();
        }
        closeWebSockets();
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

    public void setupWebSockets(String shellURL, String ioPubURL
            , AsyncHttpClient.WebSocketConnectCallback shellCallback
            , AsyncHttpClient.WebSocketConnectCallback ioPubCallback) {

        AsyncHttpClient.getDefaultInstance().websocket(shellURL, "ws", shellCallback);
        AsyncHttpClient.getDefaultInstance().websocket(ioPubURL, "ws", ioPubCallback);
    }

    public void closeWebSockets() {
        if (shellSocket != null && ioPubSocket != null) {
            shellSocket.close();
            ioPubSocket.close();
        }
        Log.i(TAG, "Sockets closed");
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
                        BaseReply reply = BaseReply.parse(s);
                        addReply(reply);
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
                        ((Activity) context).runOnUiThread(new Runnable() {
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
