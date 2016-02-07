package org.noorganization.instalistsynch.network.api.authorized;

import org.noorganization.instalist.model.Category;
import org.noorganization.instalistsynch.model.network.response.CategoryResponse;
import org.noorganization.instalistsynch.model.network.response.RetrofitUUIDResponse;

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
public interface ICategoryApiService {

    //region path groups/{id}/categories

    /**
     * Get the categories since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of categories changed since this date.
     */
    @GET("groups/{id}/categories")
    Call<List<CategoryResponse>> getCategories(@Path("id") int _id, @Query("changedSince") String _sinceTimeString);

    /**
     * Create a category with the given id.
     *
     * @param _category the category to create.
     * @return the uuid of the created category.
     */
    @POST("groups/{id}/categories")
    Call<RetrofitUUIDResponse> createCategory(@Path("id") int _id, @Body Category _category);

    //endregion

    //region path groups/{id}/categories/{uuid}

    /**
     * Get a category by its id.
     *
     * @param _uuid the uuid of the affected category.
     * @return the associated category.
     */
    @GET("groups/{id}/categories/{uuid}")
    Call<CategoryResponse> getCategory(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the category by the given category.
     *
     * @param _uuid     the uuid of the affected category.
     * @param _category the category with updates.
     * @return the uuid of the updated category.
     */
    @PUT("groups/{id}/categories/{uuid}")
    Call<RetrofitUUIDResponse> updateCategory(@Path("id") int _id, @Path("uuid") String _uuid, @Body Category _category);

    /**
     * Delete the category from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected category.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/categories/{uuid}")
    Call<RetrofitUUIDResponse> deleteCategory(@Path("id") int _id, @Path("uuid") String _uuid);

    //endregion
}
