package org.sagemath.droid;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import org.sagemath.droid.interacts.InteractView;
import org.sagemath.droid.models.database.Cell;
import org.sagemath.droid.models.gson.BaseReply;
import org.sagemath.droid.models.gson.InteractReply;

public class OutputView
        extends LinearLayout
        implements SageSingleCell.OnSageListener
        , InteractView.OnInteractListener
        , OutputBlock.OnHtmlLoadedListener {
    private final static String TAG = "SageDroid:OutputView";

    public interface onSageListener {
        public void onSageInteractListener(InteractReply interact, String name, Object value);

        public void onSageFinishedListener();
    }

    private onSageListener listener;

    private String savedHtml;

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

    public void setOutputBlocks(String html) {
        block = null;

        OutputBlock newBlock = new OutputBlock(context, cell, html);
        newBlock.reload();
        Log.i(TAG, "Creatng new block with HTML: " + html);
        block = newBlock;
    }

    private OutputBlock newOutputBlock() {
        Log.i(TAG, "Creating newOutputBlock");
        OutputBlock newBlock = new OutputBlock(context, cell);
        Log.i(TAG, "Block data: " + newBlock.getHtml());
        addView(newBlock);
        block = newBlock;
        //block.setHistoryHTML();
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
                block.setOnHtmlLoadedListener(OutputView.this);
                Log.i(TAG, "Setting block output: " + output);
                block.set(output);
            }
            if (additionalOutput != null) {
                OutputBlock block = getOutputBlock();
                block.setOnHtmlLoadedListener(OutputView.this);
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


    /**
     * Reload html from SageActivity on orientation change
     */
    public void setSavedHtml(String savedHtml) {
        removeAllViews();
        block = null;
        OutputBlock outputBlock = new OutputBlock(context, cell);
        outputBlock.reloadHtml(savedHtml);
    }

    public void clear() {
        removeAllViews();
        block = null;
        //TODO html null here
    }

    @Override
    public void onHtmlLoaded(String html) {
        savedHtml = html;
    }

    public String getSavedHtml() {
        if (savedHtml != null) {
            return savedHtml;
        }
        return null;
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
