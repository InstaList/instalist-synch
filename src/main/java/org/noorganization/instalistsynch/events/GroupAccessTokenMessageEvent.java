package org.noorganization.instalistsynch.events;

/**
 * This event is send when a temporary GroupAccessToken was requested.
 * Created by tinos_000 on 02.02.2016.
 */
public class GroupAccessTokenMessageEvent {
    private String mGroupAccessToken;

    public GroupAccessTokenMessageEvent(String _groupAccessToken) {
        mGroupAccessToken = _groupAccessToken;
    }

    public String getGroupAccessToken() {
        return mGroupAccessToken;
    }

    public void setGroupAccessToken(String groupAccessToken) {
        mGroupAccessToken = groupAccessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupAccessTokenMessageEvent)) return false;

        GroupAccessTokenMessageEvent that = (GroupAccessTokenMessageEvent) o;

        return getGroupAccessToken().equals(that.getGroupAccessToken());

    }

    @Override
    public int hashCode() {
        return getGroupAccessToken().hashCode();
    }
}
