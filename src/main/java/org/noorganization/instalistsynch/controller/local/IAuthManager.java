package org.noorganization.instalistsynch.controller.local;

/**
 * Manager to manage the authorization. Manages network and local db persistence.
 * Created by Desnoo on 06.02.2016.
 */
public interface IAuthManager {

    /**
     * Requests the token for the given device id in the specified group.
     *
     * @param _groupId    the id of the group.
     * @param _deviceUUID the uuid of the device.
     */
    void requestToken(int _groupId, String _deviceUUID);

    /**
     * Invalidate the token for the given group and device uuid.
     *
     * @param _groupId    the id of the group.
     * @param _deviceUUID the uuid of the device.
     */
    void invalidateToken(int _groupId, String _deviceUUID);
}
