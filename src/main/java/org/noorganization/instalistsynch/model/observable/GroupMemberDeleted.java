package org.noorganization.instalistsynch.model.observable;

import org.noorganization.instalistsynch.model.GroupMember;

/**
 * The observable object when it is deleted or it failed.
 * Created by Desnoo on 04.02.2016.
 */
public class GroupMemberDeleted {

    private GroupMember mGroupMember;
    private boolean mAuthorized;

    public GroupMemberDeleted(GroupMember groupMember, boolean authorized) {
        mGroupMember = groupMember;
        mAuthorized = authorized;
    }

    public GroupMember getGroupMember() {
        return mGroupMember;
    }

    public boolean isAuthorized() {
        return mAuthorized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupMemberDeleted that = (GroupMemberDeleted) o;

        if (mAuthorized != that.mAuthorized) return false;
        return !(mGroupMember != null ? !mGroupMember.equals(that.mGroupMember) : that.mGroupMember != null);

    }

    @Override
    public int hashCode() {
        int result = mGroupMember != null ? mGroupMember.hashCode() : 0;
        result = 31 * result + (mAuthorized ? 1 : 0);
        return result;
    }
}
