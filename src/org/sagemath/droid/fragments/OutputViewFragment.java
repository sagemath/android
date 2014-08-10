package org.sagemath.droid.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.squareup.otto.Subscribe;
import org.sagemath.droid.R;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.events.InteractFinishEvent;
import org.sagemath.droid.events.ProgressEvent;
import org.sagemath.droid.events.ServerDisconnectEvent;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.utils.BusProvider;
import org.sagemath.droid.views.OutputView;
import org.sagemath.droid.views.OutputWebView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author Nikhil Peter Raj
 */
public class OutputViewFragment extends BaseFragment {
    private static String TAG = "SageDroid:OutputViewFragment";
    private OutputView outputView;
    private ToggleButton outputViewToggleButton;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        outputView.register();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
        outputView.unregisterComponentViews();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_output_view, container);
        outputView = (OutputView) view.findViewById(R.id.outputView);
        outputViewToggleButton = (ToggleButton) view.findViewById(R.id.outputViewStateToggleButton);
        outputViewToggleButton.setTypeface(fontAwesome);
        return view;
    }

    @Override
    public void setCell(Cell cell) {
        super.setCell(cell);
        outputView.setCell(cell);
    }

    public void saveOutputToImage() {
        Toast.makeText(getActivity(), getString(R.string.toast_image_generating), Toast.LENGTH_SHORT).show();
        FileOutputStream fos;
        File file;
        try {
            if (isExternalStorageMounted()) {
                file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        , StringConstants.FILE_IMAGE_OUTPUT);
                fos = new FileOutputStream(file);
            } else {
                Toast.makeText(getActivity()
                        , getString(R.string.toast_external_storage_unmounted)
                        , Toast.LENGTH_SHORT).show();
                return;
            }
            OutputWebView webView = outputView.getOutputBlock();
            webView.buildDrawingCache(true);
            Bitmap outputBitmap = webView.getDrawingCache(true);
            try {
                if (outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)) {
                    fos.close();
                    final Uri outputFileUri = Uri.fromFile(file);
                    Log.i(TAG, "Uri constructed:" + outputFileUri.toString());
                    Intent imageIntent = new Intent(Intent.ACTION_SEND);
                    imageIntent.setType("image/png");
                    imageIntent.putExtra(Intent.EXTRA_STREAM, outputFileUri);
                    startActivity(imageIntent);
                }
            } finally {
                webView.destroyDrawingCache();
            }
        } catch (Exception e) {
            Log.e(TAG, e + "");
        }
    }

    private boolean isExternalStorageMounted() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    public OutputView getOutputView() {
        return outputView;
    }

    public ToggleButton getOutputViewToggleButton() {
        return outputViewToggleButton;
    }

    @Subscribe
    public void onComputationFinished(InteractFinishEvent event) {
        outputView.enableInteractViews();
        outputViewToggleButton.requestFocus();
        outputViewToggleButton.setEnabled(true);
    }

    @Subscribe
    public void onServerDisconnect(ServerDisconnectEvent event) {
        outputView.disableInteractViews();
    }

    @Subscribe
    public void onProgressUpdate(ProgressEvent event) {
        Log.i(TAG, "Received Progress Update" + event.getProgressState());
        if (event.getProgressState().equals(StringConstants.ARG_PROGRESS_START)) {
            if (!outputViewToggleButton.isEnabled()) {
                outputViewToggleButton.setEnabled(false);
            }
            outputView.disableInteractViews();
        } else if (event.getProgressState().equals(StringConstants.ARG_PROGRESS_END)) {
            outputView.enableInteractViews();
        }
    }

}
