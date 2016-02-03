package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupMember;

/**
 * Manages group related operations.
 * Works asynch, because of that no return values. Instead eventmessages will be sent.
 * Created by tinos_000 on 29.01.2016.
 */
public interface IGroupManager {

    /**
     * Creates a new group.
     * Sends following messages {@link ErrorMessageEvent} and {@link TokenMessageEvent}.
     * @param _deviceName
     */
    void createGroup(String _deviceName);

    /**
     * Join a given group.
     * @param _tmpGroupId the temporary generated group access id.
     * @param _deviceName the device name.
     * @param _isLocal indicates if this is a local group or an remote one.
     */
    void joinGroup(String _tmpGroupId,  String _deviceName, boolean _isLocal);

    /**
     * Leave the given group.
     * @param _groupAuth the group to delete.
     */
    void leaveGroup(GroupAuth _groupAuth);

    /**
     * Gets an auth token for the given group/groupAuth object.
     * @param _groupAuth the group auth object.
     */
    void requestAuthToken(GroupAuth _groupAuth);

    /**
     * Requests an temporary GroupAccessToken.
     * @param _authToken the auth token for the group to get the access token.
     * Sends an {@link org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent}.
     */
    void requestGroupAccessToken(String _authToken);


    /**
     * Get all group members with the given auth token
     * @param _authToken the auth token to the group.
     * Creates an GroupMemberListMessageEvent.
     */
    void getGroupMembers(String _authToken);

    /**
     * Triggers an update of the groupmembers.
     */
    void updateGroupMembers();

    /**
     * deletes the given groupmember from the server.
     * @param _groupMember the member to be deleted.
     * @param _token
     */
    void deleteGroupMember(GroupMember _groupMember, String _token);

    /**
     * approve the given groupmember.
     * @param _groupMember the groupmember to approve to the list.
     * @param _token
     */
    void authorizeGroupMember(GroupMember _groupMember, String _token);
}
