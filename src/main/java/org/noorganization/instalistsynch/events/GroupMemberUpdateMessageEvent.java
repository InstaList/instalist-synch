package org.noorganization.instalistsynch.events;

/**
 * Sent when a group member was updated.
 * Created by tinos_000 on 10.02.2016.
 */
public class GroupMemberUpdateMessageEvent {
    public int mGroupId;


    /**
     * Indicates that a group member was updated.
     *
     * @param _groupId the id of the updated group.
     */
    public GroupMemberUpdateMessageEvent(int _groupId) {
        mGroupId = _groupId;
    }
}
