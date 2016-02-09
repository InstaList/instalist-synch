package org.noorganization.instalistsynch.events;

/**
 * This event is send when a temporary GroupAccessToken was requested.
 * Created by tinos_000 on 02.02.2016.
 */
public class GroupAccessTokenMessageEvent {

    private int mGroupId;
    private String mGroupAccessToken;

    public GroupAccessTokenMessageEvent(int _groupId, String _groupAccessToken) {
        mGroupAccessToken = _groupAccessToken;
        mGroupId = _groupId;
    }

    public String getGroupAccessToken() {
        return mGroupAccessToken;
    }

    public void setGroupAccessToken(String groupAccessToken) {
        mGroupAccessToken = groupAccessToken;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupAccessTokenMessageEvent)) return false;

        GroupAccessTokenMessageEvent that = (GroupAccessTokenMessageEvent) o;

        if (getGroupId() != that.getGroupId()) return false;
        return !(getGroupAccessToken() != null ? !getGroupAccessToken().equals(that.getGroupAccessToken()) : that.getGroupAccessToken() != null);

    }

    @Override
    public int hashCode() {
        int result = getGroupId();
        result = 31 * result + (getGroupAccessToken() != null ? getGroupAccessToken().hashCode() : 0);
        return result;
    }
}
