package org.sagemath.droid.models;

/**
 * Created by Haven on 29-05-2014.
 */
public class PythonOutputContent {

    private PythonOutputData data;
    private int execution_count;
    private MetaData metadata;

    public PythonOutputData getData() {
        return data;
    }

    public int getExecutionCount() {
        return execution_count;
    }
}
