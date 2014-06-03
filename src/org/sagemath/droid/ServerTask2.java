package org.sagemath.droid;

import android.util.Log;
import com.google.gson.Gson;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.socketio.ConnectCallback;
import com.koushikdutta.async.http.socketio.SocketIOClient;
import com.squareup.okhttp.MediaType;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.sagemath.droid.models.WebSocketResponse;
import org.sagemath.droid.utils.UrlUtils;
import org.sagemath.singlecellserver.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;

/**
 * @author Haven
 */
public class ServerTask2 {
    private final static String TAG = "SageDroid:ServerTask2";

    private static final String HEADER_ACCEPT_ENCODING = "accept_encoding";
    private static final String HEADER_TOS = "accepted_tos";
    private static final String HEADER_USER_AGENT = "user-agent";
    private static final String VALUE_USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36";
    private static final String VALUE_IDENTITY = "identity";

    public static final MediaType jsonMediaType = MediaType.parse("application/json; charset=utf-8");

    private String session;
    private String input;
    private CommandRequest currentRequest;
    private SageSingleCell.OnSageListener listener;


    private Gson gson;
    private WebSocketResponse webSocketResponse;
    private DefaultHttpClient httpClient;


    public ServerTask2(String session, String input) {
        this.session = session;
        this.input = input;

        gson = new Gson();
    }

    public ServerTask2(String session) {
        this.session = session;

        gson = new Gson();
    }

    public void addReply(CommandReply reply) {

        Log.i(TAG, "Received CommandReply: " + reply);

        //If it contains an image file.
        if (reply instanceof DataFile) {
            try {
                DataFile dataFile;
                dataFile = (DataFile) reply;
                //dataFile.downloadFile(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (reply.containsOutput() && reply.isReplyTo(currentRequest)) {
            //TODO CommandOutput is redundant
            CommandOutput output = (CommandOutput) reply;
            listener.onSageAdditionalOutputListener(output);
        }


    }

    public WebSocketResponse sendInitialRequest() throws IOException {

        httpClient = new DefaultHttpClient();

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

        return webSocketResponse;

    }

    public void setupWebSockets(ExecuteRequest request, String shellURL, String ioPubURL, ConnectCallback shellCallback, ConnectCallback ioPubCallback) {

        SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), shellURL, shellCallback);
        SocketIOClient.connect(AsyncHttpClient.getDefaultInstance(), ioPubURL, ioPubCallback);
    }


}
