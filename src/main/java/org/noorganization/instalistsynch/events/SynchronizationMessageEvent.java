package org.noorganization.instalistsynch.events;

/**
 * The synchronizationMessageEvent is sent when the synchronization was successfull or interrupted.
 * Created by tinos_000 on 30.01.2016.
 */
public class SynchronizationMessageEvent {

    /**
     * The affected model class
     */
    public Class mClass;

    /**
     * Indicates if synch was successful or not.
     */
    public boolean mSuccessful;

    /**
     * Message to be displayed by on error.
     */
    public int mStringResourceId;

    /**
     * Constructor.
     * @param _Class the class which was synched.
     * @param _successful true if successful else false.
     */
    public SynchronizationMessageEvent(Class _Class, boolean _successful, int _stringResourceId) {
        mClass = _Class;
        mSuccessful = _successful;
        mStringResourceId = _stringResourceId;
    }
}
