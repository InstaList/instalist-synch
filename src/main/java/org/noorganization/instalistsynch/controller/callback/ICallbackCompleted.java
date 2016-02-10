package org.noorganization.instalistsynch.controller.callback;

/**
 * Callback when a network request was finally parsed. Use it for a callback of unauthorized callbacks.
 * Created by tinos_000 on 08.02.2016.
 */
public interface ICallbackCompleted<T> {

    /**
     * Called when the request was finally done.
     *
     * @param _next the object that was parsed.
     */
    void onCompleted(T _next);

    /**
     * The error when an error happened.
     *
     * @param _e the error.
     */
    void onError(Throwable _e);
}
