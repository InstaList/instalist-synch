package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.network.response.GroupResponse;

import rx.Observable;

/**
 * Manager that handles network interaction for authentification and authorization.
 * Created by Desnoo on 06.02.2016.
 */
public interface IAuthManagerNetwork {

    /**
     * Request an auth token.
     * @param _groupId the id of the group.
     * @param _groupAuth the object that holds the whole auth information.
     * @return the auth token as a Observable stream.
     */
    Observable<String> requestAuthToken(int _groupId, GroupAuth _groupAuth);

}
