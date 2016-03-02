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

import org.noorganization.instalist.comm.message.TagInfo;
import org.noorganization.instalist.model.Tag;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit interface to access the api for the {@link Tag}.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link Tag}.
 * Created by Desnoo on 06.02.2016.
 */
public interface ITagApiService {

    /**
     * Get the tags since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of tags changed since this date.
     */
    @GET("groups/{id}/tags")
    Call<List<TagInfo>> getList(@Path("id") int _id,
            @Query("changedsince") String _sinceTimeString);

    /**
     * Create a Tag with the given id.
     *
     * @param _Tag the Tag to create.
     * @return the uuid of the created Tag.
     */
    @POST("groups/{id}/tags")
    Call<Void> createItem(@Path("id") int _id, @Body TagInfo _Tag);

    /**
     * Get a Tag by its id.
     *
     * @param _uuid the uuid of the affected Tag.
     * @return the associated Tag.
     */
    @GET("groups/{id}/tags/{uuid}")
    Call<TagInfo> getItem(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the Tag by the given Tag.
     *
     * @param _uuid the uuid of the affected Tag.
     * @param _Tag  the Tag with updates.
     * @return the uuid of the updated Tag.
     */
    @PUT("groups/{id}/tags/{uuid}")
    Call<Void> updateItem(@Path("id") int _id, @Path("uuid") String _uuid, @Body TagInfo _Tag);

    /**
     * Delete the Tag from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected Tag.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/tags/{uuid}")
    Call<Void> deleteItem(@Path("id") int _id, @Path("uuid") String _uuid);

}
