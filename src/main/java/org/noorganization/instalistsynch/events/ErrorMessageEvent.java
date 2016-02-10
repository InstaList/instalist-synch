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

    private int mResourceId;

    /**
     * Default Constructor.
     *
     * @param mErrorMessage the error message to be sent, consider to use a translated string.
     */
    public ErrorMessageEvent(String mErrorMessage) {
        this.mErrorMessage = mErrorMessage;
    }

    /**
     * Create the error with an resource id where the message is.
     *
     * @param _resourceId the id of the resource.
     */
    public ErrorMessageEvent(int _resourceId) {
        mResourceId = _resourceId;
    }


    public int getResourceId() {
        return mResourceId;
    }

    /**
     * Get the error message.
     *
     * @return the error message.
     */
    public String getErrorMessage() {
        return mErrorMessage;
    }
}
