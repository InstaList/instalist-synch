package org.noorganization.instalistsynch.events;

/**
 * Event is fired when an response delivered an status code not suited.
 * Created by tinos_000 on 10.02.2016.
 */
public class HttpResponseCodeErrorMessageEvent {
    public int mCode;

    public HttpResponseCodeErrorMessageEvent(int _code) {
        mCode = _code;
    }
}
