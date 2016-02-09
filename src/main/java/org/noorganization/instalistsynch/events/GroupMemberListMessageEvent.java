package org.noorganization.instalistsynch.events;

import org.noorganization.instalistsynch.model.GroupMember;

import java.util.List;

/**
 * Message that is send when the request for Groupmembers to a group was made.
 * Created by tinos_000 on 02.02.2016.
 */
public class GroupMemberListMessageEvent {
    public List<GroupMember> mGroupMembers;
    public int mGroupId;

    public GroupMemberListMessageEvent(List<GroupMember> groupMembers, int deviceId) {
        mGroupMembers = groupMembers;
        mGroupId = deviceId;
    }
}
