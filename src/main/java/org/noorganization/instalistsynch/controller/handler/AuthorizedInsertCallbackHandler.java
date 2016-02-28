package org.noorganization.instalistsynch.controller.handler;

import android.util.Log;

import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;
import org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent;
import org.noorganization.instalistsynch.utils.GlobalObjects;
import org.noorganization.instalistsynch.utils.NetworkUtils;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Callback handler that delivers information about the insertion to the server.
 * Created by Desnoo on 22.02.2016.
 */
public class AuthorizedInsertCallbackHandler<T> implements Callback<T> {
    private static final String TAG = "AuthInsertCbHandler";
    private static int MAX_RETRIES = 3;

    /**
     * The callback object.
     */
    protected IAuthorizedInsertCallbackCompleted<T> mICallbackCompleted;
    private int mGroupId;
    private Call<T> mCall;
    private int mRetries;

    /**
     * Constructor.
     *
     * @param _groupId           the id of the group. It is necessary for the unauthorized callback.
     * @param _callbackCompleted where the callback should be directed to.
     * @param _call              the call that was made.
     */
    public AuthorizedInsertCallbackHandler(int _groupId,
                                           IAuthorizedInsertCallbackCompleted<T> _callbackCompleted, Call<T> _call) {
        mGroupId = _groupId;
        mICallbackCompleted = _callbackCompleted;
        mCall = _call;
        mRetries = 0;
    }

    @Override
    public void onResponse(Call<T> _call, Response<T> response) {
        Log.i(TAG, "onResponse: " + _call.request().method());
        if (!NetworkUtils.isSuccessful(response)) {
            switch (response.code()) {
                case 401:
                    //unauthorized
                    // check if there is not another call to not initiate a new fetch of a new access token for this group.
                    if (GlobalObjects.sCallMapping.get(mGroupId) == null
                            || !GlobalObjects.sCallMapping.get(mGroupId)) {
                        EventBus.getDefault()
                                .post(new UnauthorizedErrorMessageEvent(mGroupId, -1));
                    }
                    mICallbackCompleted.onUnauthorized(mGroupId);
                    break;
                case 409:
                    mICallbackCompleted.onConflict();
                    break;
                default:
                    mICallbackCompleted.onError(new Throwable(
                            "Other invalid network request: response code: " + String.valueOf(
                                    response.code() + " msg: " + response.message())));
            }
            return;
        }

        mICallbackCompleted.onCompleted(response.body());
    }

    @Override
    public void onFailure(Call<T> _call, Throwable t) {
        Log.e(TAG, "onResponse: " + _call.request().method(), t);
        if (mRetries > MAX_RETRIES) {
            ++mRetries;
            mCall.clone()
                    .enqueue(this);
        } else {
            mICallbackCompleted.onError(t);
        }
    }
}
