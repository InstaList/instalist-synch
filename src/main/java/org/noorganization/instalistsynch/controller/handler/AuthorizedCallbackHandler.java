package org.noorganization.instalistsynch.controller.handler;

import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent;
import org.noorganization.instalistsynch.utils.GlobalObjects;
import org.noorganization.instalistsynch.utils.NetworkUtils;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles the retrofit response and delivers the appropriate  results as a callback.
 * Has a retry policy, to request the resource 3 times.
 * Created by tinos_000 on 08.02.2016.
 */
public class AuthorizedCallbackHandler<T> implements Callback<T> {
    private static int MAX_RETRIES = 3;

    /**
     * The callback object.
     */
    protected IAuthorizedCallbackCompleted<T> mICallbackCompleted;
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
    public AuthorizedCallbackHandler(int _groupId, IAuthorizedCallbackCompleted<T> _callbackCompleted, Call<T> _call) {
        mGroupId = _groupId;
        mICallbackCompleted = _callbackCompleted;
        mCall = _call;
        mRetries = 0;
    }

    @Override
    public void onResponse(Response<T> response) {
        if (!NetworkUtils.isSuccessful(response)) {
            if (response.code() == 401) {
                //unauthorized
                // check if there is not another call to not initiate a new fetch of a new access token for this group.
                if (GlobalObjects.sCallMapping.get(mGroupId) == null || !GlobalObjects.sCallMapping.get(mGroupId))
                    EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(mGroupId, -1));
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
        if (mRetries > MAX_RETRIES) {
            ++mRetries;
            mCall.clone().enqueue(this);
        } else {
            mICallbackCompleted.onError(t);
        }
    }
}
