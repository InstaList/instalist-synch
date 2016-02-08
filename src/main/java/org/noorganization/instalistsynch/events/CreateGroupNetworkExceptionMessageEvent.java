package org.noorganization.instalistsynch.events;

/**
 * Sent when the creation of the group failed, caused by a network error.
 * Created by tinos_000 on 08.02.2016.
 */
public class CreateGroupNetworkExceptionMessageEvent {
    public String mDeviceName;
    public int mAttempt;

    public CreateGroupNetworkExceptionMessageEvent(String _deviceName, int _attempt) {
        mDeviceName = _deviceName;
        mAttempt = _attempt;
    }
}
