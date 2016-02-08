package org.noorganization.instalistsynch.controller.handler;

import org.noorganization.instalistsynch.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.utils.NetworkUtils;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Callback handler for unauthorized calls.
 * Created by tinos_000 on 08.02.2016.
 */
public class UnauthorizedCallbackHandler<T> implements Callback<T> {

    protected ICallbackCompleted<T> mICallbackCompleted;

    /**
     * Constructor.
     *
     * @param _callbackCompleted where the callback should be directed to.
     */
    public UnauthorizedCallbackHandler(ICallbackCompleted<T> _callbackCompleted) {
        mICallbackCompleted = _callbackCompleted;
    }

    @Override
    public void onResponse(Response<T> response) {
        if (!NetworkUtils.isSuccessful(response)) {
            mICallbackCompleted.onError(new Throwable("Other invalid network request: response code: " + String.valueOf(response.code())));
            return;
        }

        mICallbackCompleted.onCompleted(response.body());
    }

    @Override
    public void onFailure(Throwable t) {
        mICallbackCompleted.onError(t);
    }
}