package org.sagemath.singlecellserver;

import android.os.AsyncTask;
import android.util.Log;
import com.codebutler.android_websockets.WebSocketClient;
import com.google.gson.Gson;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.WebSocket;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.sagemath.droid.ServerTask2;
import org.sagemath.droid.models.PermalinkResponse;
import org.sagemath.droid.models.WebSocketResponse;
import org.sagemath.droid.utils.UrlUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.UUID;


/**
 * Interface with the Sage single cell server
 *
 * @author vbraun
 */
public class SageSingleCell {
    private final static String TAG = "SageDroid:SageSingleCell";

    private long timeout = 30 * 1000;
    private String permalinkURL;
    ServerTask task;

    protected boolean downloadDataFiles = true;
    private String initialRequestString;

    /**
     * Whether to immediately download data files or only save their URI
     *
     * @param value Download immediately if true
     */
    public void setDownloadDataFiles(boolean value) {
        Log.i(TAG, "Tried to setDownloadDataFiles set to " + String.valueOf(value));
        downloadDataFiles = value;
    }

    public interface OnSageListener {

        /**
         * Output in a new block or an existing block where all current entries are supposed to be erased
         *
         * @param output
         */
        public void onSageOutputListener(CommandOutput output);

        /**
         * Output to add to an existing output block
         *
         * @param output
         */
        public void onSageAdditionalOutputListener(CommandOutput output);

        /**
         * Callback for an interact_prepare message
         *
         * @param interact The interact
         */
        public void onSageInteractListener(Interact interact);


        /**
         * The Sage session has been closed
         *
         * @param reason A SessionEnd message or a HttpError
         */
        public void onSageFinishedListener(CommandReply reason);
    }

    private OnSageListener listener;

    /**
     * Set the result callback, see {@link #query(String)}
     *
     * @param listener
     */
    public void setOnSageListener(OnSageListener listener) {
        this.listener = listener;
    }

    public static String streamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static class SageInterruptedException extends Exception {
        private static final long serialVersionUID = -5638564842011063160L;
    }

    LinkedList<ServerTask> threads = new LinkedList<ServerTask>();

    public class ServerTask {
        private final static String TAG = "SageDroid:ServerTask";

        private UUID session;
        private String sageInput;
        private boolean sendOnly = false;
        private final boolean sageMode = true;
        private boolean interrupt = false;
        private DefaultHttpClient httpClient;
        private Interact interact;
        private CommandRequest request, currentRequest;
        private LinkedList<String> outputBlocks = new LinkedList<String>();
        private long initialTime = System.currentTimeMillis();
        protected WebSocketClient shellclient;
        protected WebSocketClient iopubclient;
        private String kernel_url;
        private String shell_url;
        private String iopub_url;

        private Gson gson = new Gson();
        private ServerTask2 myTask;

        protected LinkedList<CommandReply> result = new LinkedList<CommandReply>();

        protected void addReply(CommandReply reply) {

            Log.i(TAG, "Adding Reply: " + reply);
            if (reply instanceof DataFile) {
                try {
                    ((DataFile) reply).downloadFile(this);
                } catch (Exception e) {
                    Log.e(TAG, "Error download file:");
                    e.printStackTrace();
                }
            }
            Log.i(TAG, "reply.isReplyTo(currentRequest): " + String.valueOf(reply.isReplyTo(currentRequest)));
            result.add(reply);
            if (reply.isInteract()) {
                Log.i(TAG, "Reply is an interact.");
                interact = (Interact) reply;
                listener.onSageInteractListener(interact);
            } else if (reply.containsOutput() && reply.isReplyTo(currentRequest)) {
                Log.i(TAG, "Reply is response to currentRequest.");
                CommandOutput output = (CommandOutput) reply;
                if (outputBlocks.contains(output.outputBlock())) {
                    Log.i(TAG, "Output contains an output block");
                    listener.onSageAdditionalOutputListener(output);
                } else {
                    Log.i(TAG, "Added an output block");
                    outputBlocks.add(output.outputBlock());
                    listener.onSageOutputListener(output);
                }
            }
        }

        private void init() {
            Log.i(TAG, "SageSingleCell.init() called");

            HttpParams params = new BasicHttpParams();
            params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
            httpClient = new DefaultHttpClient(params);

            currentRequest = request = new ExecuteRequest(sageInput, sageMode, session);

            try {
                new shareTask().execute(sageInput);
            } catch (Exception e) {
                Log.e(TAG, "Error getting Share URI" + e.getLocalizedMessage());
            }
        }

        public ServerTask() {
            this.sageInput = null;
            this.session = null;
            init();
        }

        public ServerTask(String sageInput) {
            this.sageInput = sageInput;
            this.session = null;
            myTask = new ServerTask2(sageInput);

            init();
        }

        public void interrupt() {
            interrupt = true;
        }

        public boolean isInterrupted() {
            return interrupt;
        }

        protected class shareTask extends AsyncTask<String, Void, Void> {
            @Override
            protected Void doInBackground(String... strings) {

                try {
                    PermalinkResponse response = myTask.sendPermalinkRequest(strings[0]);
                    permalinkURL = response.getQueryURL();
                    Log.i(TAG, "Permalink URL:" + permalinkURL);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }

        }

        protected class postTask extends AsyncTask<String, Void, Void> {
            @Override
            protected Void doInBackground(String... requests) {
                initialRequestString = requests[0];
                Log.i(TAG, "SageSingleCell: postTask() called\n");

                try {
                    WebSocketResponse response = myTask.sendInitialRequest();

                    myTask.sendInitialRequestTest();

                    if (response.isValidResponse()) {
                        Log.i(TAG, "Response is valid,Setting up Websockets");
                        String shellURL = UrlUtils.getShellURL(response.getKernelID(), response.getWebSocketURL());
                        String ioPubURL = UrlUtils.getIoPubURL(response.getKernelID(), response.getWebSocketURL());

                        myTask.setupWebSockets(shellURL, ioPubURL, shellCallback, ioPubCallback);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }

        protected URI downloadFileURI(CommandReply reply, String filename) throws URISyntaxException {
            Log.i(TAG, "SageSingleCell.downloadFileURI called for " + filename);
            String fileurl = kernel_url.replace("ws", "http") + "files/" + filename;
            Log.i(TAG, "Final URI is: " + fileurl);
            return new URI(fileurl);
        }

        protected HttpResponse downloadFile(URI uri)
                throws IOException, SageInterruptedException {
            Log.i(TAG, "downloadFile called with URI " + uri.toString());
            if (interrupt) throw new SageInterruptedException();
            HttpGet httpGet = new HttpGet(uri);
            return httpClient.execute(httpGet);
        }

        protected boolean downloadDataFiles() {
            return SageSingleCell.this.downloadDataFiles;
        }

        public void start() {
            Log.i(TAG, "SageSingleCell run() called");
            request.sendRequest(this);
            return;
        }

        private WebSocketConnectCallback shellCallback = new WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception e, WebSocket webSocket) {
                //Send the execute_request
                Log.i(TAG, "Shell Connected, Sending " + initialRequestString);
                webSocket.send(initialRequestString);

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        Log.i(TAG, "Shell Received Message" + s);
                    }
                });

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception e) {
                        Log.i(TAG, "Shell Closed");
                    }
                });
            }
        };

        private WebSocketConnectCallback ioPubCallback = new WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception e, WebSocket webSocket) {
                Log.i(TAG, "IOPub Connected");
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {

                        Log.i(TAG, "IOPub received String" + s);
                        try {
                            JSONObject JSONreply = new JSONObject(s);
                            CommandReply reply = CommandReply.parse(JSONreply);
                            addReply(reply);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                });
                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception e) {
                        Log.i(TAG, "IOPub Closed");
                    }
                });
            }
        };

    }


    /**
     * Start an asynchronous query on the Sage server
     * The result will be handled by the callback set by {@link #setOnSageListener(OnSageListener)}
     *
     * @param sageInput
     */
    public void query(String sageInput) {
        Log.i(TAG, "sageInput is " + sageInput);
        task = new ServerTask(sageInput);
        task.start();
    }

    public URI getShareURI() {
        return URI.create(permalinkURL);
    }

    /**
     * Update an interactive element
     *
     * @param interact The interact_prepare message we got from the server as we set up the interact
     * @param name     The name of the variable in the interact function declaration
     * @param value    The new value
     */
    public void interact(Interact interact, String name, Object value) {
        Log.i(TAG, "UPDATING INTERACT VARIABLE: " + name);
        Log.i(TAG, "UPDATED INTERACT VALUE: " + value.toString());

        String sageInput =
                "sys._sage_.update_interact(\"" + interact.getID() +
                        "\",\"" + name +
                        "\"," + value.toString() + ")";

        task.currentRequest = task.request = new ExecuteRequest(sageInput, true, interact.session);

        String message = "";

        try {
            message = task.request.toJSON().toString();
            Log.i(TAG, "Sending Interact Update " + task.request.toJSON().toString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        task.shellclient.send(message);
    }

    /**
     * Interrupt all pending Sage server transactions
     */
    public void interrupt() {
        synchronized (threads) {
            for (ServerTask thread : threads)
                thread.interrupt();
        }
    }


    /**
     * Whether a computation is currently running
     *
     * @return
     */
    public boolean isRunning() {
        synchronized (threads) {
            for (ServerTask thread : threads)
                if (!thread.isInterrupted())
                    return true;
        }
        return false;
    }

}