package org.noorganization.instalistsynch.events;

import org.noorganization.instalistsynch.model.GroupMember;

import java.util.List;

/**
 * Message that is send when the request for Groupmembers to a group was made.
 * Created by tinos_000 on 02.02.2016.
 */
public class GroupMemberListMessageEvent {
    private List<GroupMember> mGroupMembers;
    private String mDeviceId;

    public GroupMemberListMessageEvent(List<GroupMember> groupMembers, String deviceId) {
        mGroupMembers = groupMembers;
        mDeviceId = deviceId;
    }

    public List<GroupMember> getGroupMembers() {
        return mGroupMembers;
    }

    public void setGroupMembers(List<GroupMember> groupMembers) {
        mGroupMembers = groupMembers;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }
}
