package org.noorganization.instalistsynch.events;

/**
 * Message is sent when the server responds with a 401-Unauthorized message.
 * Created by Desnoo on 06.02.2016.
 */
public class UnauthorizedErrorMessageEvent {

    /**
     * The id of the group.
     */
    private int mGroupId;

    /**
     * The id of the device.
     */
    private int mDeviceId;


    /**
     * Constructor.
     *
     * @param _groupId  the id of the group.
     * @param _deviceId the id of the device. set to -1 if no device is given.
     */
    public UnauthorizedErrorMessageEvent(int _groupId, int _deviceId) {
        mGroupId = _groupId;
        mDeviceId = _deviceId;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(int deviceId) {
        mDeviceId = deviceId;
    }
}