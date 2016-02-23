package org.noorganization.instalistsynch.controller.network.api.authorized;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.model.ShoppingList;

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
    Call<List<ListInfo>> getList(@Path("id") int _id,
            @Query("changedSince") String _sinceTimeString);

    /**
     * Create a Item with the given id.
     *
     * @param _Item the Item to create.
     * @return the uuid of the created Item.
     */
    @POST("groups/{id}/lists")
    Call<Void> createItem(@Path("id") int _id, @Body ListInfo _Item);

    //endregion

    //region Path groups/{id}/lists/{uuid}

    /**
     * Get a Item by its id.
     *
     * @param _uuid the uuid of the affected Item.
     * @return the associated Item.
     */
    @GET("groups/{id}/lists/{uuid}")
    Call<ListInfo> getItem(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the Item by the given Item.
     *
     * @param _uuid the uuid of the affected Item.
     * @param _Item the Item with updates.
     * @return the uuid of the updated Item.
     */
    @PUT("groups/{id}/lists/{uuid}")
    Call<Void> updateItem(@Path("id") int _id, @Path("uuid") String _uuid, @Body ListInfo _Item);

    /**
     * Delete the Item from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected Item.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/lists/{uuid}")
    Call<Void> deleteItem(@Path("id") int _id, @Path("uuid") String _uuid);
    //endregion
}
