package org.noorganization.instalistsynch.controller.network.api.authorized;

import org.noorganization.instalist.comm.message.EntryInfo;
import org.noorganization.instalist.model.ListEntry;

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
 * Retrofit interface to access the api for the {@link ListEntry}.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link ListEntry}.
 * Created by Desnoo on 06.02.2016.
 */
public interface IListEntryApiService {

    /**
     * Get the listentries since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of listentries changed since this date.
     */
    @GET("groups/{id}/listentries")
    Call<List<EntryInfo>> getList(@Path("id") int _id,
            @Query("changedsince") String _sinceTimeString);

    /**
     * Create a ListEntry with the given id.
     *
     * @param _ListEntry the ListEntry to create.
     * @return the uuid of the created ListEntry.
     */
    @POST("groups/{id}/listentries")
    Call<Void> createItem(@Path("id") int _id, @Body EntryInfo _ListEntry);

    /**
     * Get a ListEntry by its id.
     *
     * @param _uuid the uuid of the affected ListEntry.
     * @return the associated ListEntry.
     */
    @GET("groups/{id}/listentries/{uuid}")
    Call<EntryInfo> getItem(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the ListEntry by the given ListEntry.
     *
     * @param _uuid      the uuid of the affected ListEntry.
     * @param _ListEntry the ListEntry with updates.
     * @return the uuid of the updated ListEntry.
     */
    @PUT("groups/{id}/listentries/{uuid}")
    Call<Void> updateItem(@Path("id") int _id, @Path("uuid") String _uuid,
            @Body EntryInfo _ListEntry);

    /**
     * Delete the ListEntry from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected ListEntry.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/listentries/{uuid}")
    Call<Void> deleteItem(@Path("id") int _id, @Path("uuid") String _uuid);

}
