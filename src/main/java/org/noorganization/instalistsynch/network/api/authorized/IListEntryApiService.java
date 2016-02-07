package org.noorganization.instalistsynch.network.api.authorized;

import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalistsynch.model.network.response.ListEntryResponse;
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
 * Retrofit interface to access the api for the {@link ListEntry}.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link ListEntry}.
 * Created by Desnoo on 06.02.2016.
 */
public interface IListEntryApiService {

    /**
     * Get the listEntries since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of listEntries changed since this date.
     */
    @GET("groups/{id}/listEntries")
    Call<List<ListEntryResponse>> getListEntries(@Path("id") int _id, @Query("changedSince") String _sinceTimeString);

    /**
     * Create a ListEntry with the given id.
     *
     * @param _ListEntry the ListEntry to create.
     * @return the uuid of the created ListEntry.
     */
    @POST("groups/{id}/listEntries")
    Call<RetrofitUUIDResponse> createListEntry(@Path("id") int _id, @Body ListEntry _ListEntry);

    /**
     * Get a ListEntry by its id.
     *
     * @param _uuid the uuid of the affected ListEntry.
     * @return the associated ListEntry.
     */
    @GET("groups/{id}/listEntries/{uuid}")
    Call<ListEntryResponse> getListEntry(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the ListEntry by the given ListEntry.
     *
     * @param _uuid      the uuid of the affected ListEntry.
     * @param _ListEntry the ListEntry with updates.
     * @return the uuid of the updated ListEntry.
     */
    @PUT("groups/{id}/listEntries/{uuid}")
    Call<RetrofitUUIDResponse> updateListEntry(@Path("id") int _id, @Path("uuid") String _uuid, @Body ListEntry _ListEntry);

    /**
     * Delete the ListEntry from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected ListEntry.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/listEntries/{uuid}")
    Call<RetrofitUUIDResponse> deleteListEntry(@Path("id") int _id, @Path("uuid") String _uuid);

}
