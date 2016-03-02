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

import org.noorganization.instalistsynch.events.ErrorMessageEvent;

/**
 * The manager for groups. Processes network and db interaction.
 * Created by Desnoo on 07.02.2016.
 */
public interface IGroupManagerController {

    /**
     * Creates a new group.
     * Sends following messages {@link ErrorMessageEvent}.
     *
     * @param _deviceName the name of the device.
     */
    void createGroup(String _deviceName);

    /**
     * Join a given group.
     *
     * @param _groupAccessKey the temporary generated group access id.
     * @param _deviceName     the device name.
     * @param _isLocal        indicates if this is a local group or an remote one.
     * @param _groupId        the id of the group.
     */
    void joinGroup(String _groupAccessKey, String _deviceName, boolean _isLocal, int _groupId);

    /**
     * Leave the given group.
     *
     * @param _groupId  the group to delete.
     * @param _deviceId  the id of the device.
     */
    void deleteMemberOfGroup(int _groupId, int _deviceId);

    /**
     * Requests an temporary GroupAccessToken.
     *
     * @param _groupId the auth token for the group to get the access token.
     *                 Sends an {@link org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent}.
     */
    void requestGroupAccessToken(int _groupId);

    /**
     * Get all group members with the given auth token
     *
     * @param _groupId id of the group.
     * Creates an GroupMemberListMessageEvent.
     */
    void getGroupMembers(int _groupId);

    /**
     * Refresh all group members.
     */
    void refreshGroupMember();

    /**
     * approve the given groupmember.
     *
     * @param _groupId  the id of the group.
     * @param _deviceId the id of the device.
     */
    void authorizeGroupMember(int _groupId, int _deviceId);
}
