package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.utils.NetworkUtils;

import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles the retrofit response and delivers the appropriate  results as a callback.
 * Created by tinos_000 on 08.02.2016.
 */
public class AuthorizedCallbackHandler<T> implements Callback<T> {

    /**
     * The callback object.
     */
    protected IAuthorizedCallbackCompleted<T> mICallbackCompleted;
    private int mGroupId;

    /**
     * Constructor.
     *
     * @param _groupId           the id of the group. It is necessary for the unauthorized callback.
     * @param _callbackCompleted where the callback should be directed to.
     */
    public AuthorizedCallbackHandler(int _groupId, IAuthorizedCallbackCompleted<T> _callbackCompleted) {
        mGroupId = _groupId;
        mICallbackCompleted = _callbackCompleted;
    }

    @Override
    public void onResponse(Response<T> response) {
        if (!NetworkUtils.isSuccessful(response)) {
            if (response.code() == 401) {
                //unauthorized
                mICallbackCompleted.onUnauthorized(mGroupId);
            } else {
                mICallbackCompleted.onError(new Throwable("Other invalid network request: response code: " + String.valueOf(response.code())));
            }
            return;
        }

        mICallbackCompleted.onCompleted(response.body());
    }

    @Override
    public void onFailure(Throwable t) {
        mICallbackCompleted.onError(t);
    }
}
