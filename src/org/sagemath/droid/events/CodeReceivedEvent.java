package org.sagemath.droid.events;

/**
 * Created by Haven on 08-07-2014.
 */
public class CodeReceivedEvent {
    private String receivedCode;
    private boolean forRun;

    public boolean isForRun() {
        return forRun;
    }

    public void setForRun(boolean forRun) {
        this.forRun = forRun;
    }

    public CodeReceivedEvent(String receivedCode) {
        this.receivedCode = receivedCode;
    }

    public String getReceivedCode() {
        return receivedCode;
    }
}
