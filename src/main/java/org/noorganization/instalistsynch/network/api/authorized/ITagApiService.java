package org.noorganization.instalistsynch.network.api.authorized;

import org.noorganization.instalist.model.Tag;
import org.noorganization.instalistsynch.model.network.response.RetrofitUUIDResponse;
import org.noorganization.instalistsynch.model.network.response.TagResponse;

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
    Call<List<TagResponse>> getTags(@Path("id") int _id, @Query("changedSince") String _sinceTimeString);

    /**
     * Create a Tag with the given id.
     *
     * @param _Tag   the Tag to create.
     * @return the uuid of the created Tag.
     */
    @POST("groups/{id}/tags")
    Call<RetrofitUUIDResponse> createTag(@Path("id") int _id, @Body Tag _Tag);

    /**
     * Get a Tag by its id.
     *
     * @param _uuid  the uuid of the affected Tag.
     * @return the associated Tag.
     */
    @GET("groups/{id}/tags/{uuid}")
    Call<TagResponse> getTag(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the Tag by the given Tag.
     *
     * @param _uuid  the uuid of the affected Tag.
     * @param _Tag   the Tag with updates.
     * @return the uuid of the updated Tag.
     */
    @PUT("groups/{id}/tags/{uuid}")
    Call<RetrofitUUIDResponse> updateTag(@Path("id") int _id, @Path("uuid") String _uuid, @Body Tag _Tag);

    /**
     * Delete the Tag from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected Tag.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/tags/{uuid}")
    Call<RetrofitUUIDResponse> deleteTag(@Path("id") int _id, @Path("uuid") String _uuid);

}
