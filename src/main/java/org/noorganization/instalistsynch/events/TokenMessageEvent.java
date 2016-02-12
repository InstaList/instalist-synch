package org.noorganization.instalistsynch.events;

import de.greenrobot.event.EventBus;

/**
 * Messages that include the token.
 * Created by tinos_000 on 27.01.2016.
 */
public class TokenMessageEvent {

    private String mToken;
    private int mGroupId;

    public TokenMessageEvent(String _token, int _groupId) {
        mToken = _token;
        mGroupId = _groupId;
    }

    public String getmToken() {
        return mToken;
    }

    public void setmToken(String mToken) {
        this.mToken = mToken;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }
}
