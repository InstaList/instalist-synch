package org.noorganization.instalistsynch.model;

import java.util.List;

/**
 * Model for the expandable list.
 * Created by tinos_000 on 02.02.2016.
 */
public class GroupExpandableList {

    private GroupAuthAccess mGroupAuthAccess;
    private List<GroupMember> mGroupMemberList;

    public GroupExpandableList(GroupAuthAccess groupAuthAccess, List<GroupMember> groupMemberList) {
        mGroupAuthAccess = groupAuthAccess;
        mGroupMemberList = groupMemberList;
    }

    public GroupAuthAccess getGroupAuthAccess() {
        return mGroupAuthAccess;
    }

    public void setGroupAuthAccess(GroupAuthAccess groupAuthAccess) {
        mGroupAuthAccess = groupAuthAccess;
    }

    public List<GroupMember> getGroupMemberList() {
        return mGroupMemberList;
    }

    public void setGroupMemberList(List<GroupMember> groupMemberList) {
        mGroupMemberList = groupMemberList;
    }
}
