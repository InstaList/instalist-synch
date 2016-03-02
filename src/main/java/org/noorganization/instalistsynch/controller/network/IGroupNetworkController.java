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

import org.noorganization.instalist.comm.message.DeviceInfo;
import org.noorganization.instalist.comm.message.GroupInfo;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.model.GroupMember;

import java.util.List;

/**
 * Manages group related operations.
 * Works asynch, because of that no return values. Instead a observable sequence is used.
 * Created by tinos_000 on 29.01.2016.
 */
public interface IGroupNetworkController {

    /**
     * Creates a new group.
     * Sends following messages {@link ErrorMessageEvent} and {@link TokenMessageEvent}.
     *
     * @param _callbackCompleted the callback when the request is completely parsed.
     */
    void createGroup(ICallbackCompleted<GroupInfo> _callbackCompleted);

    /**
     * Join a given group.
     *
     * @param _callback         the callback when the request is done.
     * @param _groupAccessToken the access token to the group.
     * @param _deviceName       the device name.
     * @param _groupId          the id of the group.
     * @param _secret           the secret for the client.
     */
    void joinGroup(ICallbackCompleted<DeviceInfo> _callback, String _groupAccessToken, String _deviceName, int _groupId, String _secret);

    /**
     * Deletes the given user in the specified group. Can also be used to leave a group.
     *
     * @param _callback  the callback when the request is completed.
     * @param _authToken the group to delete.
     * @param _groupId   the id of the associated group.
     * @param _deviceId  @return an Observable of groupLeave elements.
     */
    void deleteMemberOfGroup(IAuthorizedCallbackCompleted<Void> _callback, String _authToken, int _groupId, int _deviceId);

    /**
     * Requests an temporary GroupAccessToken.
     *  @param _callback  the callback when the request is completed.
     * @param _groupId   the id of the group to get the access token for.
     * @param _authToken the auth token for the group to get the access token.
 *                   Sends an {@link org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent}.
     */
    void requestGroupAccessToken(IAuthorizedCallbackCompleted<GroupInfo> _callback, int _groupId, String _authToken);

    /**
     * Get all group members with the given auth token
     *
     * @param _callback the callback when the request is completed.
     * @param _groupId   the id of the group.
     * @param _authToken the auth token to the group.
     */
    void getGroupMembers(IAuthorizedCallbackCompleted<List<DeviceInfo>> _callback, int _groupId, String _authToken);

    /**
     * Approve the given groupmember.
     *
     * @param _callback the callback when the request is completed.
     * @param _groupMember the groupmember to approve to the list.
     * @param _authToken   the token to authorize.
     */
    void authorizeGroupMember(IAuthorizedCallbackCompleted<Void> _callback, GroupMember _groupMember, String _authToken);
}
