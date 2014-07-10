package org.sagemath.droid.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.widget.LinearLayout;
import org.sagemath.droid.SageSingleCell;
import org.sagemath.droid.events.InteractFinishEvent;
import org.sagemath.droid.interacts.InteractView;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.models.gson.BaseReply;
import org.sagemath.droid.models.gson.InteractReply;
import org.sagemath.droid.states.InteractViewState;
import org.sagemath.droid.states.OutputBlockState;
import org.sagemath.droid.states.OutputViewState;
import org.sagemath.droid.utils.BusProvider;

public class OutputView
        extends LinearLayout
        implements SageSingleCell.OnSageListener {
    private final static String TAG = "SageDroid:OutputView";

    private OutputWebView block;
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

    public void unregisterComponentViews() {
        if (block != null) {
            block.unregister();
        }
        if (interactView != null) {
            interactView.unregister();
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

    public OutputWebView getOutputBlock() {
        if (block != null)
            return block;
        else return newOutputBlock();

    }

    private OutputWebView newOutputBlock() {
        Log.i(TAG, "Creating newOutputBlock");
        OutputWebView newBlock = new OutputWebView(context, cell);
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
                Log.d(TAG, "Setting:  " + output.getStringMessageType());
                OutputWebView block = getOutputBlock();
                Log.i(TAG, "Setting block output: " + output.getStringMessageType());
                block.set(output);
            }
            if (additionalOutput != null) {
                OutputWebView block = getOutputBlock();
                Log.i(TAG, "Adding additionalOutput: " + additionalOutput.getStringMessageType());
                //block.clearBlocks();
                block.add(additionalOutput);
            }
            if (interact != null) {

                interactView = new InteractView(context);
                interactView.set(interact);
                Log.i(TAG, "Adding Interact view with controls: "
                        + interact.getContent().getData().getInteract().getControls());
                addView(interactView, 0);
            }
            if (finished != null) {
                Log.i(TAG, "onSageFinishedListener called.");
                BusProvider.getInstance().post(new InteractFinishEvent());
            }
        }
    }

    public void clear() {
        removeAllViews();
        block = null;
    }

    public void disableInteractViews() {
        if (interactView != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    interactView.disableViews();
                }
            });
        }

    }

    public void enableInteractViews() {
        if (interactView != null) {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    interactView.enableViews();
                }
            });
        }


    }

}
