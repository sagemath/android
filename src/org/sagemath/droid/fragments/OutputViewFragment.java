package org.sagemath.droid.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;
import com.squareup.otto.Subscribe;
import org.sagemath.droid.R;
import org.sagemath.droid.constants.StringConstants;
import org.sagemath.droid.events.InteractFinishEvent;
import org.sagemath.droid.events.ProgressEvent;
import org.sagemath.droid.events.ServerDisconnectEvent;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.utils.BusProvider;
import org.sagemath.droid.view.OutputView;

/**
 * Created by Haven on 08-07-2014.
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
