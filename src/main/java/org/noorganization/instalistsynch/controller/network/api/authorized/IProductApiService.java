package org.noorganization.instalistsynch.controller.network.api.authorized;

import org.noorganization.instalist.comm.message.ProductInfo;
import org.noorganization.instalist.model.Product;

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
 * Retrofit interface to access the api for the {@link Product}.
 * Groups/{id} is prefixed at any item to declare the unique association of the {@link Product}.
 * Created by Desnoo on 06.02.2016.
 */
public interface IProductApiService {
    //region Path groups/{id}/products

    /**
     * Get the products since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @return the list of products changed since this date.
     */
    @GET("groups/{id}/products")
    Call<List<ProductInfo>> getProducts(@Path("id") int _id,
            @Query("changedSince") String _sinceTimeString);

    /**
     * Create a Product with the given id.
     *
     * @param _Product the Product to create.
     * @return the uuid of the created Product.
     */
    @POST("groups/{id}/products")
    Call<Void> createProduct(@Path("id") int _id, @Body ProductInfo _Product);

    //endregion

    //region Path groups/{id}/products/{uuid}

    /**
     * Get a Product by its id.
     *
     * @param _uuid the uuid of the affected Product.
     * @return the associated Product.
     */
    @GET("groups/{id}/products/{uuid}")
    Call<ProductInfo> getProduct(@Path("id") int _id, @Path("uuid") String _uuid);

    /**
     * Update the Product by the given Product.
     *
     * @param _uuid    the uuid of the affected Product.
     * @param _Product the Product with updates.
     * @return the uuid of the updated Product.
     */
    @PUT("groups/{id}/products/{uuid}")
    Call<Void> updateProduct(@Path("id") int _id, @Path("uuid") String _uuid,
            @Body ProductInfo _Product);

    /**
     * Delete the Product from the remote server with the given id.
     *
     * @param _uuid the uuid of the affected Product.
     * @return the uuid of the deleted element.
     */
    @DELETE("groups/{id}/products/{uuid}")
    Call<Void> deleteProduct(@Path("id") int _id, @Path("uuid") String _uuid);

    //endregion

}
