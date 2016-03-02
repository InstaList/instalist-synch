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

package org.noorganization.instalistsynch.controller.network.api.unauthorized;

import org.noorganization.instalist.comm.message.DeviceInfo;
import org.noorganization.instalist.comm.message.DeviceRegistration;
import org.noorganization.instalist.comm.message.GroupInfo;
import org.noorganization.instalist.comm.message.TokenInfo;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * All Api interactions that does not require any authorization.
 * Created by Desnoo on 06.02.2016.
 */
public interface IUnauthorizedApiService {

    /**
     * Try to register a new group.
     *
     * @return an temporary group-access-id.
     */
    @POST("groups")
    Call<GroupInfo> registerGroup();


    /**
     * Register this device to a specific group.
     *
     * @param _registration the registration object.
     * @return the registered device id in this group.
     */
    @POST("groups/{id}/devices")
    Call<DeviceInfo> registerDevice(@Path("id") int _id, @Body DeviceRegistration _registration);


    /**
     * Get the token for the group.
     *
     * @param _authorization Authorization-header as defined per RFC 2617. Expects the server defined user-id as user and the client defined secret as password. "Basic " + client:password
     * @return a token for this specific group.
     */
    @GET("groups/{id}/devices/token")
    Call<TokenInfo> token(@Path("id") int _id, @Header("Authorization") String _authorization);
}
