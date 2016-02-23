package org.noorganization.instalistsynch.controller.network.api.authorized;

import org.noorganization.instalist.comm.message.UnitInfo;
import org.noorganization.instalist.model.Unit;

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
 * Retrofit interface to access the api for the {@link Unit}.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link Unit}.
 * Created by Desnoo on 06.02.2016.
 */
public interface IUnitApiService {

    /**
     * Get the units since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of units changed since this date.
     */
    @GET("groups/{id}/units")
    Call<List<UnitInfo>> getList(@Path("id") int _id,
            @Query("changedSince") String _sinceTimeString);

    /**
     * Create a Unit with the given id.
     *
     * @param _Unit the Unit to create.
     * @return the uuid of the created Unit.
     */
    @POST("groups/{id}/units")
    Call<Void> createItem(@Path("id") int _id, @Body UnitInfo _Unit);

    /**
     * Get a Unit by its id.
     *
     * @param _uuid the uuid of the affected Unit.
     * @return the associated Unit.
     */
    @GET("groups/{id}/units/{uuid}")
    Call<UnitInfo> getItem(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the Unit by the given Unit.
     *
     * @param _uuid the uuid of the affected Unit.
     * @param _Unit the Unit with updates.
     * @return the uuid of the updated Unit.
     */
    @PUT("groups/{id}/units/{uuid}")
    Call<Void> updateItem(@Path("id") int _id, @Path("uuid") String _uuid, @Body UnitInfo _Unit);

    /**
     * Delete the Unit from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected Unit.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/units/{uuid}")
    Call<Void> deleteItem(@Path("id") int _id, @Path("uuid") String _uuid);
}
