package org.noorganization.instalistsynch.controller.local;

/**
 * Manager to manage the authorization. Manages network and local db persistence.
 * Created by Desnoo on 06.02.2016.
 */
public interface IAuthManagerController {

    /**
     * Requests the token for the given device id in the specified group.
     *
     * @param _groupId    the id of the group.
     */
    void requestToken(int _groupId);

    /**
     * Invalidate the token for the given group and device uuid.
     *
     * @param _groupId    the id of the group.
     */
    void invalidateToken(int _groupId);
}
