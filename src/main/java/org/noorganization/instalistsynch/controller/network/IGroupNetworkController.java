package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.network.response.GroupAccessKey;
import org.noorganization.instalistsynch.model.network.response.GroupMemberRetrofit;
import org.noorganization.instalistsynch.model.network.response.GroupResponse;
import org.noorganization.instalistsynch.model.network.response.RegisterDeviceResponse;

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
     * @param _callbackCompleted the callback when the request was completely parsed.
     */
    void createGroup(ICallbackCompleted<GroupResponse> _callbackCompleted);

    /**
     * Join a given group.
     *
     * @param _callback         the callback when the request was done.
     * @param _groupAccessToken the access token to the group.
     * @param _deviceName       the device name.
     * @param _groupId          the id of the group.
     * @param _secret           the secret for the client.
     */
    void joinGroup(ICallbackCompleted<RegisterDeviceResponse> _callback, String _groupAccessToken, String _deviceName, int _groupId, String _secret);

    /**
     * Deletes the given user in the specified group. Can also be used to leave a group.
     *
     * @param _callback  the callback when the request was completed.
     * @param _authToken the group to delete.
     * @param _groupId   the id of the associated group.
     * @param _deviceId  @return an Observable of groupLeave elements.
     */
    void deleteMemberOfGroup(IAuthorizedCallbackCompleted<Void> _callback, String _authToken, int _groupId, int _deviceId);

    /**
     * Requests an temporary GroupAccessToken.
     *
     * @param _callback  the callback when the request was completed.
     * @param _groupId   the id of the group to get the access token for.
     * @param _authToken the auth token for the group to get the access token.
     *                   Sends an {@link org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent}.
     */
    void requestGroupAccessToken(IAuthorizedCallbackCompleted<GroupAccessKey> _callback, int _groupId, String _authToken);

    /**
     * Get all group members with the given auth token
     *
     * @param _groupId   the id of the group.
     * @param _authToken the auth token to the group.
     *                   Creates an GroupMemberListMessageEvent.
     */
    void getGroupMembers(IAuthorizedCallbackCompleted<List<GroupMemberRetrofit>> _callback, int _groupId, String _authToken);

    /**
     * Approve the given groupmember.
     *
     * @param _groupMember the groupmember to approve to the list.
     * @param _authToken   the token to authorize.
     */
    void authorizeGroupMember(IAuthorizedCallbackCompleted<Void> _callback, GroupMember _groupMember, String _authToken);
}
