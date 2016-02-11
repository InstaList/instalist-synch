package org.noorganization.instalistsynch.controller.network.impl;

import android.util.Log;

import org.noorganization.instalist.comm.message.TokenInfo;
import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.handler.UnauthorizedCallbackHandler;
import org.noorganization.instalistsynch.controller.network.IAuthNetworkController;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.utils.ApiUtils;
import org.noorganization.instalistsynch.utils.RFC2617Authorization;

import retrofit2.Call;

/**
 * Controller that handles the authorization of the network interaction.
 * Created by tinos_000 on 08.02.2016.
 */
public class AuthNetworkController implements IAuthNetworkController {
    private static final String LOG_TAG = AuthNetworkController.class.getSimpleName();
    private static AuthNetworkController sInstance;

    /**
     * Get the instance of this class.
     *
     * @return the instance.
     */
    public static synchronized AuthNetworkController getInstance() {
        if (sInstance == null)
            sInstance = new AuthNetworkController();

        return sInstance;
    }

    private AuthNetworkController() {

    }

    @Override
    public void requestAuthToken(ICallbackCompleted<TokenInfo> _callback, GroupAuth _groupAuth) {
        Call<TokenInfo> call = ApiUtils.getInstance().getUnauthorizedInstantListApiService().token(_groupAuth.getGroupId(), RFC2617Authorization
                .generate(_groupAuth.getDeviceId(), _groupAuth.getSecret()));
        call.enqueue(new UnauthorizedCallbackHandler<>(_callback, call));
        Log.i(LOG_TAG, "requestAuthToken: ");
    }
}
