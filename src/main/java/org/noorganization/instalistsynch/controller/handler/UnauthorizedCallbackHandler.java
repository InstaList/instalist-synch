package org.noorganization.instalistsynch.controller.handler;

import android.util.Log;

import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.utils.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Callback handler for unauthorized calls.
 * Has a retry policy, to request the resource 3 times.
 * Created by tinos_000 on 08.02.2016.
 */
public class UnauthorizedCallbackHandler<T> implements Callback<T> {
    private static final String TAG = "UnauthorizedCbHandler";
    private static int MAX_RETRIES = 3;
    protected ICallbackCompleted<T> mICallbackCompleted;
    private Call<T> mCall;
    private int mRetries;

    /**
     * Constructor.
     *
     * @param _callbackCompleted where the callback should be directed to.
     * @param _call              the call that was made.
     */
    public UnauthorizedCallbackHandler(ICallbackCompleted<T> _callbackCompleted, Call<T> _call) {
        mICallbackCompleted = _callbackCompleted;
        mCall = _call;
        mRetries = 0;
    }

    @Override
    public void onResponse(Call<T> _call, Response<T> response) {
        Log.i(TAG, "onResponse: " + _call.request().method());
        if (!NetworkUtils.isSuccessful(response)) {
            mICallbackCompleted.onError(new Throwable("Other invalid network request: response code: " + String.valueOf(response.code())));
            return;
        }

        mICallbackCompleted.onCompleted(response.body());
    }

    @Override
    public void onFailure(Call<T> _call, Throwable t) {
        Log.e(TAG, "onResponse: " + _call.request().method(), t);
        if (mRetries > MAX_RETRIES) {
            ++mRetries;
            mCall.clone().enqueue(this);
        } else {
            mICallbackCompleted.onError(t);
        }
    }
}