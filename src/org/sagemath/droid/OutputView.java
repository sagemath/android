package org.sagemath.droid;

import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.widget.LinearLayout;
import org.sagemath.droid.interacts.InteractView;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.models.gson.BaseReply;
import org.sagemath.droid.models.gson.InteractReply;
import org.sagemath.droid.states.InteractViewState;
import org.sagemath.droid.states.OutputBlockState;
import org.sagemath.droid.states.OutputViewState;

public class OutputView
        extends LinearLayout
        implements SageSingleCell.OnSageListener
        , InteractView.OnInteractListener {
    private final static String TAG = "SageDroid:OutputView";

    public interface onSageListener {
        public void onSageInteractListener(InteractReply interact, String name, Object value);

        public void onSageFinishedListener();
    }

    private onSageListener listener;

    public void setOnSageListener(onSageListener listener) {
        this.listener = listener;
    }

    private OutputBlock block;
    private InteractView interactView;

    private Context context;
    private Cell cell;

    public OutputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setOrientation(VERTICAL);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        OutputBlockState blockState = null;
        InteractViewState viewState = null;
        Log.i(TAG, "In onSaveInstanceState");
        if (block != null) {
            blockState = new OutputBlockState(superState, block.getHtmlData());
        }
        if (interactView != null) {
            viewState = new InteractViewState(superState, interactView.getAddedViews());
        }

        if (blockState == null && viewState == null) {
            //Contains neither html output nor interacts, just return normal
            Log.i(TAG, "No output, default behaviour");
            return superState;
        }

        if (viewState == null) {
            //Only HTML output
            Log.i(TAG, "Saving HTML Output");
            return blockState;
        } else {
            // Has both Interact Controls and HTML
            Log.i(TAG, "Saving Interact Output");
            return new OutputViewState(superState, blockState, viewState);
        }

    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof BaseSavedState)) {
            //No state was saved, skip
            super.onRestoreInstanceState(state);
            return;
        }
        removeAllViews();
        if (state instanceof OutputBlockState) {
            //HTML was saved, restore it.
            final OutputBlockState savedState = (OutputBlockState) state;
            super.onRestoreInstanceState(savedState.getSuperState());

            //Have to post it in a runnable or UI will not update
            handler.post(new Runnable() {
                @Override
                public void run() {
                    String html = savedState.getSavedHtml();
                    block = getOutputBlock();
                    block.setHtmlFromSavedState(html);
                }
            });
        } else if (state instanceof OutputViewState) {
            //Restore an interact state, along with HTML
            OutputViewState outputViewState = (OutputViewState) state;
            super.onRestoreInstanceState(outputViewState.getSuperState());
            final OutputBlockState blockState = outputViewState.getOutputBlockState();
            final InteractViewState viewState = outputViewState.getInteractViewState();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    //Restore html
                    block = getOutputBlock();
                    block.setHtmlFromSavedState(blockState.getSavedHtml());

                    //Restore interacts
                    interactView = new InteractView(context);
                    interactView.addInteractsFromSavedState(viewState.getSavedControls());
                    interactView.setOnInteractListener(OutputView.this);
                    addView(interactView, 0);
                }
            });


        }
    }

    //Prevent Child Views from saving/restoring their state,
    //since we will handle this explicitly
    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchSaveInstanceState(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        super.dispatchRestoreInstanceState(container);
    }

    @Override
    public void onSageReplyListener(BaseReply output) {
        Log.i(TAG, "Received Output");
        UpdateResult task = new UpdateResult();
        task.output = output;
        handler.post(task);
    }

    @Override
    public void onSageAdditionalReplyListener(BaseReply output) {
        Log.i(TAG, "Received Additional Output");
        UpdateResult task = new UpdateResult();
        task.additionalOutput = output;
        handler.post(task);
    }

    @Override
    public void onSageInteractListener(InteractReply interact) {
        UpdateResult task = new UpdateResult();
        task.interact = interact;
        handler.post(task);
    }

    @Override
    public void onSageFinishedListener(BaseReply reason) {
        UpdateResult task = new UpdateResult();
        task.finished = reason;
        handler.post(task);
    }

    @Override
    public void onInteractUpdated() {
        block.clearBlocks();
    }

    private Handler handler = new Handler();

    public OutputBlock getOutputBlock() {
        if (block != null)
            return block;
        else return newOutputBlock();

    }

    private OutputBlock newOutputBlock() {
        Log.i(TAG, "Creating newOutputBlock");
        OutputBlock newBlock = new OutputBlock(context, cell);
        Log.i(TAG, "Block data: " + newBlock.getHtmlData());
        addView(newBlock);
        block = newBlock;
        return block;
    }

    private class UpdateResult implements Runnable {
        private BaseReply output;
        private BaseReply additionalOutput;
        private InteractReply interact;
        private BaseReply finished;

        @Override
        public void run() {

            if (output != null) {
                Log.d(TAG, "set " + output.toString());
                OutputBlock block = getOutputBlock();
                Log.i(TAG, "Setting block output: " + output);
                block.set(output);
            }
            if (additionalOutput != null) {
                OutputBlock block = getOutputBlock();
                Log.i(TAG, "Adding additionalOutput: " + additionalOutput);
                //block.clearBlocks();
                block.add(additionalOutput);
            }
            if (interact != null) {

                interactView = new InteractView(context);
                interactView.set(interact);
                interactView.setOnInteractListener(OutputView.this);
                Log.i(TAG, "Adding Interact view: " + interact.toString());
                addView(interactView, 0);
            }
            if (finished != null)
                Log.i(TAG, "onSageFinishedListener called.");
            listener.onSageFinishedListener();
        }
    }

    public void clear() {
        removeAllViews();
        block = null;
    }

    @Override
    public void onInteractListener(InteractReply interact, String name, Object value) {
        listener.onSageInteractListener(interact, name, value);
    }

    public void disableInteractViews() {
        if (interactView != null)
            interactView.disableViews();
    }

    public void enableInteractViews() {
        if (interactView != null)
            interactView.enableViews();

    }

}
