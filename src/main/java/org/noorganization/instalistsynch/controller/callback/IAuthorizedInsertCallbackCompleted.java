package org.noorganization.instalistsynch.controller.callback;

/**
 * Interface to achieve reaction to insert conflicts.
 * Created by Desnoo on 22.02.2016.
 */
public interface IAuthorizedInsertCallbackCompleted<T> extends IAuthorizedCallbackCompleted<T> {

    /**
     * Is called when the server responds with HTTP code 409 conflict.
     */
    void onConflict();
}
