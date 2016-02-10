package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.network.response.RetrofitAuthToken;

/**
 * Manager that handles network interaction for authentification and authorization.
 * Created by Desnoo on 06.02.2016.
 */
public interface IAuthNetworkController {

    /**
     * Request an auth token.
     *
     * @param _groupAuth the object that holds the whole auth information.
     * @return the auth token as a Observable stream.
     */
    void requestAuthToken(ICallbackCompleted<RetrofitAuthToken> _callback, GroupAuth _groupAuth);

}
