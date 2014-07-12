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
import org.sagemath.droid.database.SageSQLiteOpenHelper;
import org.sagemath.droid.events.CodeReceivedEvent;
import org.sagemath.droid.events.InteractFinishEvent;
import org.sagemath.droid.events.ProgressEvent;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.view.CodeView;
import org.sagemath.droid.utils.BusProvider;

/**
 * Created by Haven on 08-07-2014.
 */
public class CodeEditorFragment extends BaseFragment {
    private static final String TAG = "SageDroid:CodeEditorFragment";

    private static final String ARG_CURRENT_INPUT = "currentInput";

    private CodeView codeView;
    private ToggleButton codeViewToggleButton;
    private String currentInput = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_code_editor, container);
        codeView = (CodeView) view.findViewById(R.id.codeView);
        codeViewToggleButton = (ToggleButton) view.findViewById(R.id.codeViewStateToggleButton);
        codeViewToggleButton.setTypeface(fontAwesome);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            currentInput = savedInstanceState.getString(ARG_CURRENT_INPUT);
            setAndFocusEditor(currentInput);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentInput != null)
            outState.putString(ARG_CURRENT_INPUT, currentInput);
    }

    public CodeView getCodeView() {
        return codeView;
    }

    @Override
    public void setCell(Cell cell) {
        super.setCell(cell);
        if (cell.getInput() != null && (!cell.getInput().equals(""))) {
            setAndFocusEditor(cell.getInput());
            currentInput = cell.getInput();
        }
    }

    @Subscribe
    public void codeReceived(CodeReceivedEvent event) {
        Log.d(TAG, "Received code from editor: " + event.getReceivedCode());
        String receivedCode = event.getReceivedCode();
        cell.setInput(receivedCode);
        SageSQLiteOpenHelper.getInstance(getActivity()).saveEditedCell(cell);
    }

    @Subscribe
    public void onComputationFinished(InteractFinishEvent event) {
        codeViewToggleButton.requestFocus();
        codeViewToggleButton.setEnabled(true);
    }

    private void setAndFocusEditor(String text) {
        Log.i(TAG, "Setting Text:" + text);
        codeView.setEditorText(text);
    }

    @Subscribe
    public void onProgressUpdate(ProgressEvent event) {
        if (event.getProgressState().equals(StringConstants.ARG_PROGRESS_START)) {
            if (!codeViewToggleButton.isEnabled()) {
                codeViewToggleButton.setEnabled(false);
            }
        } else if (event.getProgressState().equals(StringConstants.ARG_PROGRESS_END)) {
        }
    }

    public ToggleButton getCodeViewToggleButton() {
        return codeViewToggleButton;
    }

    public void setSavedInput(String savedInput) {
        codeView.setEditorText(savedInput);
    }

}
