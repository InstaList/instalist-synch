package org.noorganization.instalistsynch.controller.network.api.authorized;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalistsynch.model.network.response.ShoppingListResponse;

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
 * Retrofit interface to access the api for the {@link ShoppingList}.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link ShoppingList}.
 * Created by Desnoo on 06.02.2016.
 */
public interface IListApiService {
    //region Path groups/{id}/lists

    /**
     * Get the lists since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of lists changed since this date.
     */
    @GET("groups/{id}/lists")
    Call<List<ListInfo>> getShoppingLists(@Path("id") int _id, @Query("changedSince") String _sinceTimeString);

    /**
     * Create a ShoppingList with the given id.
     *
     * @param _ShoppingList the ShoppingList to create.
     * @return the uuid of the created ShoppingList.
     */
    @POST("groups/{id}/lists")
    Call<Void> createShoppingList(@Path("id") int _id, @Body ListInfo _ShoppingList);

    //endregion

    //region Path groups/{id}/lists/{uuid}

    /**
     * Get a ShoppingList by its id.
     *
     * @param _uuid the uuid of the affected ShoppingList.
     * @return the associated ShoppingList.
     */
    @GET("groups/{id}/lists/{uuid}")
    Call<ListInfo> getShoppingList(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the ShoppingList by the given ShoppingList.
     *
     * @param _uuid         the uuid of the affected ShoppingList.
     * @param _ShoppingList the ShoppingList with updates.
     * @return the uuid of the updated ShoppingList.
     */
    @PUT("groups/{id}/lists/{uuid}")
    Call<Void> updateShoppingList(@Path("id") int _id, @Path("uuid") String _uuid, @Body ListInfo _ShoppingList);

    /**
     * Delete the ShoppingList from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected ShoppingList.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/lists/{uuid}")
    Call<Void> deleteShoppingList(@Path("id") int _id, @Path("uuid") String _uuid);
    //endregion
}
