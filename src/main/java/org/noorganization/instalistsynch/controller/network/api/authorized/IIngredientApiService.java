package org.noorganization.instalistsynch.controller.network.api.authorized;

import org.noorganization.instalist.comm.message.IngredientInfo;
import org.noorganization.instalist.model.Ingredient;

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
 * Retrofit interface to access the api for the {@link Ingredient}.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link Ingredient}.
 * Created by Desnoo on 06.02.2016.
 */
public interface IIngredientApiService {
    /**
     * Get the ingredients since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of ingredients changed since this date.
     */
    @GET("groups/{id}/ingredients")
    Call<List<IngredientInfo>> getList(@Path("id") int _id,
            @Query("changedsince") String _sinceTimeString);

    /**
     * Create a Ingredient with the given id.
     *
     * @param _Ingredient the Ingredient to create.
     * @return the uuid of the created Ingredient.
     */
    @POST("groups/{id}/ingredients")
    Call<Void> createItem(@Path("id") int _id, @Body IngredientInfo _Ingredient);

    /**
     * Get a Ingredient by its id.
     *
     * @param _uuid the uuid of the affected Ingredient.
     * @return the associated Ingredient.
     */
    @GET("groups/{id}/ingredients/{uuid}")
    Call<IngredientInfo> getItem(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the Ingredient by the given Ingredient.
     *
     * @param _uuid       the uuid of the affected Ingredient.
     * @param _Ingredient the Ingredient with updates.
     * @return the uuid of the updated Ingredient.
     */
    @PUT("groups/{id}/ingredients/{uuid}")
    Call<Void> updateItem(@Path("id") int _id, @Path("uuid") String _uuid,
            @Body IngredientInfo _Ingredient);

    /**
     * Delete the Ingredient from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected Ingredient.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/ingredients/{uuid}")
    Call<Void> deleteItem(@Path("id") int _id, @Path("uuid") String _uuid);

}
