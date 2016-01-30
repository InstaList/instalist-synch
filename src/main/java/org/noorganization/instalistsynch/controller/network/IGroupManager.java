package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuth;

/**
 * Manages group related operations.
 * Created by tinos_000 on 29.01.2016.
 */
public interface IGroupManager {

    /**
     * Creates a new group.
     * Sends following messages {@link ErrorMessageEvent} and {@link TokenMessageEvent}.
     */
    void createGroup();

    /**
     * Join a given group.
     * @param _tmpGroupId the temporary generated group access id.
     */
    void joinGroup(String _tmpGroupId);

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

}
