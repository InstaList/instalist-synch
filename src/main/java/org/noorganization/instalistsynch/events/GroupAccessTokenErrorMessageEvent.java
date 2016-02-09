package org.noorganization.instalistsynch.events;

/**
 * Sent when the access token for a group cannot be fetched.
 * Created by tinos_000 on 09.02.2016.
 */
public class GroupAccessTokenErrorMessageEvent {
    public String mMsg;

    public GroupAccessTokenErrorMessageEvent(String _msg) {
        mMsg = _msg;
    }
}
