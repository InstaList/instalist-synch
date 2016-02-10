package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalist.comm.message.TokenInfo;
import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.model.GroupAuth;

/**
 * Manager that handles network interaction for authentification and authorization.
 * Created by Desnoo on 06.02.2016.
 */
public interface IAuthNetworkController {

    /**
     * Request an auth token.
     *
     * @param _groupAuth the object that holds the whole auth information.
     */
    void requestAuthToken(ICallbackCompleted<TokenInfo> _callback, GroupAuth _groupAuth);

}
