package org.noorganization.instalistsynch.network.api.authorized;

import org.noorganization.instalistsynch.model.network.response.GroupMemberRetrofit;
import org.noorganization.instalistsynch.model.network.response.GroupResponse;

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
    Call<List<GroupMemberRetrofit>> getDevicesOfGroup(@Path("id") int _id);

    /**
     * Updates the given groupmember.
     *
     * @param _groupMembers the groupmember list to be updated.
     * @return nothing.
     */
    @PUT("groups/{id}/devices/{deviceid}")
    Call<Void> updateDeviceOfGroup(@Path("id") int _id, @Path("deviceid") int _deviceId, @Body List<GroupMemberRetrofit> _groupMembers);


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
    Call<GroupResponse> getGroupAccessKey(@Path("id") int _id);
}
