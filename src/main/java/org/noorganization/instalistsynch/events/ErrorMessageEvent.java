package org.noorganization.instalistsynch.events;

/**
 * Sent when an error occured.
 * Created by tinos_000 on 29.01.2016.
 */
public class ErrorMessageEvent {

    /**
     * The message of the error.
     */
    private String mErrorMessage;

    /**
     * Default Constructor.
     * @param mErrorMessage the error message to be sent, consider to use a translated string.
     */
    public ErrorMessageEvent(String mErrorMessage) {
        this.mErrorMessage = mErrorMessage;
    }

    /**
     * Get the error message.
     * @return the error message.
     */
    public String getErrorMessage() {
        return mErrorMessage;
    }
}
