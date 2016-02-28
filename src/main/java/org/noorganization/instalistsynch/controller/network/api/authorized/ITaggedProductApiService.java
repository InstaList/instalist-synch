package org.noorganization.instalistsynch.controller.network.api.authorized;

import org.noorganization.instalist.comm.message.TaggedProductInfo;
import org.noorganization.instalist.model.TaggedProduct;

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
 * Retrofit interface to access the api for the {@link TaggedProduct}'s.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link TaggedProduct}.
 * Created by Desnoo on 06.02.2016.
 */
public interface ITaggedProductApiService {

    /**
     * Get the taggedproducts since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of taggedproducts changed since this date.
     */
    @GET("groups/{id}/taggedproducts")
    Call<List<TaggedProductInfo>> getList(@Path("id") int _id,
            @Query("changedsince") String _sinceTimeString);

    /**
     * Create a TaggedProduct with the given id.
     *
     * @param _TaggedProduct the TaggedProduct to create.
     * @return the uuid of the created TaggedProduct.
     */
    @POST("groups/{id}/taggedproducts")
    Call<Void> createItem(@Path("id") int _id, @Body TaggedProductInfo _TaggedProduct);

    /**
     * Get a TaggedProduct by its id.
     *
     * @param _uuid the uuid of the affected TaggedProduct.
     * @return the associated TaggedProduct.
     */
    @GET("groups/{id}/taggedproducts/{uuid}")
    Call<TaggedProductInfo> getItem(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the TaggedProduct by the given TaggedProduct.
     *
     * @param _uuid          the uuid of the affected TaggedProduct.
     * @param _TaggedProduct the TaggedProduct with updates.
     * @return the uuid of the updated TaggedProduct.
     */
    @PUT("groups/{id}/taggedproducts/{uuid}")
    Call<Void> updateItem(@Path("id") int _id, @Path("uuid") String _uuid,
            @Body TaggedProductInfo _TaggedProduct);

    /**
     * Delete the TaggedProduct from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected TaggedProduct.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/taggedproducts/{uuid}")
    Call<Void> deleteItem(@Path("id") int _id, @Path("uuid") String _uuid);
}
