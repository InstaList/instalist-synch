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

import org.noorganization.instalist.comm.message.CategoryInfo;
import org.noorganization.instalist.model.Category;

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
 * Retrofit interface to access the api for the {@link Category}.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link Category}.
 * Created by Desnoo on 06.02.2016.
 */
public interface ICategoryApiService{


    //region path groups/{id}/categories

    /**
     * Get the categories since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of categories changed since this date.
     */
    @GET("groups/{id}/categories")
    Call<List<CategoryInfo>> getList(@Path("id") int _id,
            @Query("changedsince") String _sinceTimeString);

    /**
     * Create a category with the given id.
     *
     * @param _category the category to create.
     * @return the uuid of the created category.
     */
    @POST("groups/{id}/categories")
    Call<Void> createItem(@Path("id") int _id, @Body CategoryInfo _category);

    //endregion

    //region path groups/{id}/categories/{uuid}

    /**
     * Get a category by its id.
     *
     * @param _uuid the uuid of the affected category.
     * @return the associated category.
     */
    @GET("groups/{id}/categories/{uuid}")
    Call<CategoryInfo> getItem(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the category by the given category.
     *
     * @param _uuid     the uuid of the affected category.
     * @param _category the category with updates.
     * @return the uuid of the updated category.
     */
    @PUT("groups/{id}/categories/{uuid}")
    Call<Void> updateItem(@Path("id") int _id, @Path("uuid") String _uuid, @Body
    CategoryInfo _category);

    /**
     * Delete the category from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected category.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/categories/{uuid}")
    Call<Void> deleteItem(@Path("id") int _id, @Path("uuid") String _uuid);

    //endregion
}
