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
