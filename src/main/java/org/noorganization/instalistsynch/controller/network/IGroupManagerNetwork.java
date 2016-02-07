package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.model.observable.GroupMemberDeleted;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.network.response.GroupResponse;
import org.noorganization.instalistsynch.model.observable.GroupJoin;
import org.noorganization.instalistsynch.model.observable.GroupLeave;
import org.noorganization.instalistsynch.model.observable.GroupMemberAuthorized;

import rx.Observable;

/**
 * Manages group related operations.
 * Works asynch, because of that no return values. Instead a observable sequence is used.
 * Created by tinos_000 on 29.01.2016.
 */
public interface IGroupManagerNetwork {

    /**
     * Creates a new group.
     * Sends following messages {@link ErrorMessageEvent} and {@link TokenMessageEvent}.
     *
     * @param _deviceName the name of the device.
     * @return the Observable stream with the GroupResponse including the access key.
     */
    Observable<GroupResponse> createGroup(String _deviceName);

    /**
     * Join a given group.
     *
     * @param _tmpGroupId the temporary generated group access id.
     * @param _deviceName the device name.
     * @param _isLocal    indicates if this is a local group or an remote one.
     * @return an Observable of boolean that indicates if the insertion went good or not.
     */
    Observable<GroupJoin> joinGroup(String _tmpGroupId, String _deviceName, boolean _isLocal);

    /**
     * Leave the given group.
     *
     * @param _groupAuth the group to delete.
     * @return an Observable of groupLeave elements.
     */
    Observable<GroupLeave> leaveGroup(GroupAuth _groupAuth);

    /**
     * Requests an temporary GroupAccessToken.
     *
     * @param _groupId   the id of the group to get the access token for.
     * @param _authToken the auth token for the group to get the access token.
     *                   Sends an {@link org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent}.
     * @return an Observable with groupAccessToken.
     */
    Observable<String> requestGroupAccessToken(int _groupId, String _authToken);

    /**
     * Get all group members with the given auth token
     *
     * @param _groupId   the id of the group.
     * @param _authToken the auth token to the group.
     *                   Creates an GroupMemberListMessageEvent.
     * @return an Observable with GroupMember objects.
     */
    Observable<GroupMember> getGroupMembers(int _groupId, String _authToken);

    /**
     * Triggers an update of the groupmembers.
     */
    void updateGroupMembers();

    /**
     * deletes the given groupmember from the server.
     *
     * @param _groupMember the member to be deleted.
     * @param _token       the auth token of our device.
     * @return  an Observable Sequence  of possible deleted groupmembers.
     */
    Observable<GroupMemberDeleted> deleteGroupMember(GroupMember _groupMember, String _token);

    /**
     * Approve the given groupmember.
     *
     * @param _groupMember the groupmember to approve to the list.
     * @param _token       the token to authorize.
     * @return an Observable with the GroupMemberAuthorized objects.
     */
    Observable<GroupMemberAuthorized> authorizeGroupMember(GroupMember _groupMember, String _token);
}
