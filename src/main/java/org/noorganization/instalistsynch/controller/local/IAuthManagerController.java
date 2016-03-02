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

package org.noorganization.instalistsynch.controller.local;

/**
 * Manager to manage the authorization. Manages network and local db persistence.
 * Created by Desnoo on 06.02.2016.
 */
public interface IAuthManagerController {

    /**
     * Requests the token for the given device id in the specified group.
     *
     * @param _groupId the id of the group.
     */
    void requestToken(int _groupId);

    /**
     * Loads all session tokens and gives them to the sessioncontroller.
     */
    void loadAllSessions();

    /**
     * Invalidate the token for the given group and device uuid.
     *
     * @param _groupId the id of the group.
     */
    void invalidateToken(int _groupId);
}
