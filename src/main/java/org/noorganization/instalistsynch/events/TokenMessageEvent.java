package org.noorganization.instalistsynch.events;

import de.greenrobot.event.EventBus;

/**
 * Messages that include the token.
 * Created by tinos_000 on 27.01.2016.
 */
public class TokenMessageEvent {

    private String mToken;

    public TokenMessageEvent(String _token){
        mToken = _token;
    }

    public String getmToken() {
        return mToken;
    }

    public void setmToken(String mToken) {
        this.mToken = mToken;
    }
}
