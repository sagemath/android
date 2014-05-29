package org.sagemath.droid.models;

import org.sagemath.droid.constants.ExecutionState;

/**
 * Content corresponding to kernel status messages.
 */
public class StatusContent {

    public static final String STR_BUSY = "busy";
    public static final String STR_IDLE = "idle";
    public static final String STR_DEAD = "dead";
    public static final String STR_OK = "ok"; //Not sure if this status exists

    private String execution_state;

    public int getExecutionState() {
        if (execution_state.equalsIgnoreCase(STR_BUSY))
            return ExecutionState.BUSY;
        else if (execution_state.equalsIgnoreCase(STR_IDLE))
            return ExecutionState.IDLE;
        else if (execution_state.equalsIgnoreCase(STR_DEAD))
            return ExecutionState.DEAD;
        else if(execution_state.equalsIgnoreCase(STR_OK))
            return ExecutionState.OK;
        else return ExecutionState.ERROR;

    }
}
