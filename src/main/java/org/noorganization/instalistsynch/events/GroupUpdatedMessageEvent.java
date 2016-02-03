package org.noorganization.instalistsynch.events;

import org.noorganization.instalistsynch.model.GroupMember;

/**
 * Triggered when a group was updated.
 * Created by Desnoo on 03.02.2016.
 */
public class GroupUpdatedMessageEvent {

    public GroupMember mChangedGroupMember;

    public GroupUpdatedMessageEvent(GroupMember changedGroupMember) {
        mChangedGroupMember = changedGroupMember;
    }
}
