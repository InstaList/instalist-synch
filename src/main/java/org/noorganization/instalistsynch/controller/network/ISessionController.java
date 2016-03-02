/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.model.GroupAccess;

import java.util.List;

/**
 * Manages each single session. It is an abstraction layer to interact with the api.
 * Created by Desnoo on 03.02.2016.
 */
public interface ISessionController {

    /**
     * Get the specific token for the given values.
     * If there is no token associated with this groupId an
     * {@link org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent} is sent to create a new token.
     *
     * @param _groupId the id of the group.
     * @return the token of the combination. or null if there is no token for this group.
     */
    String getToken(int _groupId);


    /**
     * Adds or updates a token for the groupId to/in the cache.
     *
     * @param _groupId the id of the group.
     * @param _token   the auth token.
     */
    void addOrUpdateToken(int _groupId, String _token);

    /**
     * Removes the token for the given group.
     *
     * @param _groupId the id of the group.
     */
    void removeToken(int _groupId);


    /**
     * Loads all tokens into the cache.
     *
     * @param _accessTokenPairs a list of all known accessTokens.
     */
    void loadToken(List<GroupAccess> _accessTokenPairs);
}
