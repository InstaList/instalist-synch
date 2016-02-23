package org.noorganization.instalistsynch.controller.network.api.authorized;

import org.noorganization.instalist.comm.message.RecipeInfo;
import org.noorganization.instalist.model.Recipe;

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
 * Retrofit interface to access the api for the {@link Recipe}.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link Recipe}.
 * Created by Desnoo on 06.02.2016.
 */
public interface IRecipeApiService {
    /**
     * Get the recipes since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of recipes changed since this date.
     */
    @GET("groups/{id}/recipes")
    Call<List<RecipeInfo>> getList(@Path("id") int _id,
            @Query("changedSince") String _sinceTimeString);

    /**
     * Create a Recipe with the given id.
     *
     * @param _Recipe the Recipe to create.
     * @return the uuid of the created Recipe.
     */
    @POST("groups/{id}/recipes")
    Call<Void> createItem(@Path("id") int _id, @Body RecipeInfo _Recipe);

    /**
     * Get a Recipe by its id.
     *
     * @param _uuid the uuid of the affected Recipe.
     * @return the associated Recipe.
     */
    @GET("groups/{id}/recipes/{uuid}")
    Call<RecipeInfo> getItem(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the Recipe by the given Recipe.
     *
     * @param _uuid   the uuid of the affected Recipe.
     * @param _Recipe the Recipe with updates.
     * @return the uuid of the updated Recipe.
     */
    @PUT("groups/{id}/recipes/{uuid}")
    Call<Void> updateItem(@Path("id") int _id, @Path("uuid") String _uuid,
            @Body RecipeInfo _Recipe);

    /**
     * Delete the Recipe from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected Recipe.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/recipes/{uuid}")
    Call<Void> deleteItem(@Path("id") int _id, @Path("uuid") String _uuid);

}
