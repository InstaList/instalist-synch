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

package org.noorganization.instalistsynch.controller.local.dba;

import org.noorganization.instalistsynch.model.TempGroupAccessToken;

/**
 * The db controller to manage temporary acess tokens.
 * Created by tinos_000 on 08.02.2016.
 */
public interface ITempGroupAccessTokenDbController {

    /**
     * Get the access token for the given group.
     *
     * @param _groupId the id of the group.
     * @return the accesstoken or null if there is no token generated.
     */
    TempGroupAccessToken getAccessToken(int _groupId);

    /**
     * Get the local access token.
     *
     * @return the accesstoken or null if there was no token generated.
     */
    TempGroupAccessToken getLocalAccessToken();

    /**
     * Deletes the access token for the given group.
     *
     * @param _groupId the id of the group.
     * @return true if it was removed successfully, else false.
     */
    boolean deleteAccessToken(int _groupId);

    /**
     * Insert the temporary access token.
     *
     * @param _groupId   the id of the group.
     * @param _accessKey the accesskey for the group.
     * @param _isLocal
     * @return true if insertion went good, else false.
     */
    boolean insertAccessToken(int _groupId, String _accessKey, boolean _isLocal);

}
