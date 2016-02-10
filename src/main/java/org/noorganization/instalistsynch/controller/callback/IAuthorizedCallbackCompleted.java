package org.noorganization.instalistsynch.controller.callback;

/**
 * Callback for
 * Created by tinos_000 on 08.02.2016.
 */
public interface IAuthorizedCallbackCompleted<T> extends ICallbackCompleted<T> {

    /**
     * Called when the user is unauthorized.
     *
     * @param _groupId the groupId where the accesstoken is not valid.
     */
    void onUnauthorized(int _groupId);
}
