package org.noorganization.instalistsynch.events;

/**
 * Event that is sent when a member was deleted from a group.
 * Created by tinos_000 on 09.02.2016.
 */
public class DeletedMemberMessageEvent {

    public int mGroupId;
    public int mDeviceId;

    public DeletedMemberMessageEvent(int _groupId, int _deviceId) {
        mGroupId = _groupId;
        mDeviceId = _deviceId;
    }
}
