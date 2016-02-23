package org.noorganization.instalistsynch.controller.network.api.authorized;

import org.noorganization.instalist.comm.message.DeviceInfo;
import org.noorganization.instalist.comm.message.GroupInfo;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * The api to access the API of the group specific interaction.
 * Created by Desnoo on 06.02.2016.
 */
public interface IGroupApiService {

    /**
     * Get all devices in the current group where the token is currently assigned.
     *
     * @return a List of associated GroupMembers.
     */
    @GET("groups/{id}/devices")
    Call<List<DeviceInfo>> getDevicesOfGroup(@Path("id") int _id);

    /**
     * Updates the given groupmember.
     *
     * @param _deviceInfo the info of the device.
     * @return nothing.
     */
    @PUT("groups/{id}/devices/{deviceid}")
    Call<Void> updateDeviceOfGroup(@Path("id") int _id, @Path("deviceid") int _deviceId, @Body DeviceInfo _deviceInfo);


    /**
     * Deletes the given groupmembers by their ids.
     *
     * @param _deviceId the ids of the groupmembers.
     * @return nothing.
     */
    @DELETE("groups/{id}/devices/{deviceid}")
    Call<Void> deleteDevicesOfGroup(@Path("id") int _id, @Path("deviceid") int _deviceId);

    /**
     * Get the temporary access key to a group.
     *
     * @return the access token to the group.
     */
    @GET("groups/{id}/access_key")
    Call<GroupInfo> getGroupAccessKey(@Path("id") int _id);
}
