package org.noorganization.instalistsynch.controller.local;

import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupMember;

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
     *  @param _groupAccessKey the temporary generated group access id.
     * @param _deviceName     the device name.
     * @param _isLocal        indicates if this is a local group or an remote one.
     * @param _groupId
     */
    void joinGroup(String _groupAccessKey, String _deviceName, boolean _isLocal, int _groupId);

    /**
     * Leave the given group.
     *
     * @param _groupAuth the group to delete.
     */
    void leaveGroup(GroupAuth _groupAuth);

    /**
     * Requests an temporary GroupAccessToken.
     *
     * @param _authToken the auth token for the group to get the access token.
     *                   Sends an {@link org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent}.
     */
    void requestGroupAccessToken(String _authToken);

    /**
     * Get all group members with the given auth token
     *
     * @param _authToken the auth token to the group.
     *                   Creates an GroupMemberListMessageEvent.
     */
    void getGroupMembers(String _authToken);

    /**
     * deletes the given groupmember from the server.
     *
     * @param _groupMember the member to be deleted.
     * @param _token
     */
    void deleteGroupMember(GroupMember _groupMember, String _token);

    /**
     * approve the given groupmember.
     *
     * @param _groupMember the groupmember to approve to the list.
     * @param _token       the token to authorize.
     */
    void authorizeGroupMember(GroupMember _groupMember, String _token);
}
