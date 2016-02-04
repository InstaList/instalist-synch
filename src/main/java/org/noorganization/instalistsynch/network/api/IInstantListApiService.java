package org.noorganization.instalistsynch.network.api;

import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Recipe;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.model.TaggedProduct;
import org.noorganization.instalist.model.Unit;
import org.noorganization.instalistsynch.model.Group;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.network.response.CategoryResponse;
import org.noorganization.instalistsynch.model.network.response.GroupMemberRetrofit;
import org.noorganization.instalistsynch.model.network.response.IngredientResponse;
import org.noorganization.instalistsynch.model.network.response.ListEntryResponse;
import org.noorganization.instalistsynch.model.network.response.ProductResponse;
import org.noorganization.instalistsynch.model.network.response.RecipeResponse;
import org.noorganization.instalistsynch.model.network.response.RetrofitAuthToken;
import org.noorganization.instalistsynch.model.network.response.RetrofitGroupAccessToken;
import org.noorganization.instalistsynch.model.network.response.RetrofitRegisterDevice;
import org.noorganization.instalistsynch.model.network.response.RetrofitUUIDResponse;
import org.noorganization.instalistsynch.model.network.response.ShoppingListResponse;
import org.noorganization.instalistsynch.model.network.response.TagResponse;
import org.noorganization.instalistsynch.model.network.response.TaggedProductResponse;
import org.noorganization.instalistsynch.model.network.response.UnitResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * This interface defines all possible api interactions with the server.
 * Created by tinos_000 on 28.01.2016.
 */
public interface IInstantListApiService {


    //region Auth

    /**
     * Try to register a new group.
     *
     * @return an temporary group-access-id.
     */
    @POST("user/register_group")
    Call<RetrofitGroupAccessToken> registerGroup();

    /**
     * Register this device to a specific group.
     *
     * @param _group the temporary group object.
     * @return the registered device id in this group.
     */
    @POST("user/register_device")
    Call<RetrofitRegisterDevice> registerDevice(@Body Group _group);

    /**
     * Get the token for the group.
     *
     * @param _authorization Authorization-header as defined per RFC 2617. Expects the server defined user-id as user and the client defined secret as password. "Basic " + client:password
     * @return a token for this specific group.
     */
    @GET("user/token")
    Call<RetrofitAuthToken> token(@Header("Authorization") String _authorization);

    //endregion

    //region Group

    /**
     * Get all devices in the current group where the token is currently assigned.
     * @param _token the auth token.
     * @return a List of associated GroupMembers.
     */
    @GET("user/group/devices")
    Call<List<GroupMemberRetrofit>> getDevicesOfGroup(@Query("token") String _token);

    /**
     * Updates the given groupmember.
     * @param _token the auth token.
     * @param _groupMembers  the groupmember list to be updated.
     * @return nothing.
     */
    @PUT("user/group/devices")
    Call<Void> updateDeviceOfGroup(@Query("token") String _token, @Body List<GroupMemberRetrofit> _groupMembers);


    /**
     * Deletes the given groupmembers by their ids.
     * @param _token the auth token.
     * @param _deviceId  the ids of the groupmembers.
     * @return nothing.
     */
    @DELETE("user/group/devices")
    Call<Void> deleteDevicesOfGroup(@Query("token") String _token, @Query("deviceid") int _deviceId);

    /**
     * Get the temporary access key to a group.
     * @param _token the auth token.
     * @return the access token to the group.
     */
    @GET("user/group/access_key")
    Call<RetrofitGroupAccessToken> getGroupAccessKey(@Query("token") String _token);

    //endregion

    //region Category

    /**
     * Get the categories since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @param _token           the auth token to identify the associated group.
     * @return the list of categories changed since this date.
     */
    @GET("categories")
    Call<List<CategoryResponse>> getCategories(@Query("token") String _token, @Query("changedSince") String _sinceTimeString);

    /**
     * Get a category by its id.
     *
     * @param _uuid  the uuid of the affected category.
     * @param _token the auth token to identify the associated group.
     * @return the associated category.
     */
    @GET("categories/{uuid}")
    Call<CategoryResponse> getCategory(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Create a category with the given id.
     *
     * @param _uuid     the uuid of the affected category.
     * @param _token    the auth token to identify the associated group.
     * @param _category the category to create.
     * @return the uuid of the created category.
     */
    @POST("categories/{uuid}")
    Call<RetrofitUUIDResponse> createCategory(@Path("uuid") String _uuid, @Query("token") String _token, @Body Category _category);

    /**
     * Update the category by the given category.
     *
     * @param _uuid     the uuid of the affected category.
     * @param _token    the auth token to identify the associated group.
     * @param _category the category with updates.
     * @return the uuid of the updated category.
     */
    @PUT("categories/{uuid}")
    Call<RetrofitUUIDResponse> updateCategory(@Path("uuid") String _uuid, @Query("token") String _token, @Body Category _category);

    /**
     * Delete the category from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected category.
     * @param _token the auth token to identify the associated group.
     * @return the uuid of the deleted element.
     */
    @DELETE("categories/{uuid}")
    Call<RetrofitUUIDResponse> deleteCategory(@Path("uuid") String _uuid, @Query("token") String _token);

    //endregion


    //region Product

    /**
     * Get the products since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @param _token           the auth token to identify the associated group.
     * @return the list of products changed since this date.
     */
    @GET("products")
    Call<List<ProductResponse>> getProducts(@Query("token") String _token, @Query("changedSince") String _sinceTimeString);

    /**
     * Get a Product by its id.
     *
     * @param _uuid  the uuid of the affected Product.
     * @param _token the auth token to identify the associated group.
     * @return the associated Product.
     */
    @GET("products/{uuid}")
    Call<ProductResponse> getProduct(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Create a Product with the given id.
     *
     * @param _uuid    the uuid of the affected Product.
     * @param _token   the auth token to identify the associated group.
     * @param _Product the Product to create.
     * @return the uuid of the created Product.
     */
    @POST("products/{uuid}")
    Call<RetrofitUUIDResponse> createProduct(@Path("uuid") String _uuid, @Query("token") String _token, @Body Product _Product);

    /**
     * Update the Product by the given Product.
     *
     * @param _uuid    the uuid of the affected Product.
     * @param _token   the auth token to identify the associated group.
     * @param _Product the Product with updates.
     * @return the uuid of the updated Product.
     */
    @PUT("products/{uuid}")
    Call<RetrofitUUIDResponse> updateProduct(@Path("uuid") String _uuid, @Query("token") String _token, @Body Product _Product);

    /**
     * Delete the Product from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected Product.
     * @param _token the auth token to identify the associated group.
     * @return the uuid of the deleted element.
     */
    @DELETE("products/{uuid}")
    Call<RetrofitUUIDResponse> deleteProduct(@Path("uuid") String _uuid, @Query("token") String _token);

    //endregion

    //region ShoppingList

    /**
     * Get the lists since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @param _token           the auth token to identify the associated group.
     * @return the list of lists changed since this date.
     */
    @GET("lists")
    Call<List<ShoppingListResponse>> getShoppingLists(@Query("token") String _token, @Query("changedSince") String _sinceTimeString);

    /**
     * Get a ShoppingList by its id.
     *
     * @param _uuid  the uuid of the affected ShoppingList.
     * @param _token the auth token to identify the associated group.
     * @return the associated ShoppingList.
     */
    @GET("lists/{uuid}")
    Call<ShoppingListResponse> getShoppingList(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Create a ShoppingList with the given id.
     *
     * @param _uuid         the uuid of the affected ShoppingList.
     * @param _token        the auth token to identify the associated group.
     * @param _ShoppingList the ShoppingList to create.
     * @return the uuid of the created ShoppingList.
     */
    @POST("lists/{uuid}")
    Call<RetrofitUUIDResponse> createShoppingList(@Path("uuid") String _uuid, @Query("token") String _token, @Body ShoppingList _ShoppingList);

    /**
     * Update the ShoppingList by the given ShoppingList.
     *
     * @param _uuid         the uuid of the affected ShoppingList.
     * @param _token        the auth token to identify the associated group.
     * @param _ShoppingList the ShoppingList with updates.
     * @return the uuid of the updated ShoppingList.
     */
    @PUT("lists/{uuid}")
    Call<RetrofitUUIDResponse> updateShoppingList(@Path("uuid") String _uuid, @Query("token") String _token, @Body ShoppingList _ShoppingList);

    /**
     * Delete the ShoppingList from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected ShoppingList.
     * @param _token the auth token to identify the associated group.
     * @return the uuid of the deleted element.
     */
    @DELETE("lists/{uuid}")
    Call<RetrofitUUIDResponse> deleteShoppingList(@Path("uuid") String _uuid, @Query("token") String _token);

    //endregion

    //region Recipe

    /**
     * Get the recipes since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @param _token           the auth token to identify the associated group.
     * @return the list of recipes changed since this date.
     */
    @GET("recipes")
    Call<List<RecipeResponse>> getRecipes(@Query("token") String _token, @Query("changedSince") String _sinceTimeString);

    /**
     * Get a Recipe by its id.
     *
     * @param _uuid  the uuid of the affected Recipe.
     * @param _token the auth token to identify the associated group.
     * @return the associated Recipe.
     */
    @GET("recipes/{uuid}")
    Call<RecipeResponse> getRecipe(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Get a Recipe by its id.
     *
     * @param _uuid  the uuid of the affected Recipe.
     * @param _token the auth token to identify the associated group.
     * @return the associated Recipe.
     */
    @GET("recipes/{uuid}/ingredients")
    Call<List<IngredientResponse>> getRecipeIngredients(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Create a Recipe with the given id.
     *
     * @param _uuid   the uuid of the affected Recipe.
     * @param _token  the auth token to identify the associated group.
     * @param _Recipe the Recipe to create.
     * @return the uuid of the created Recipe.
     */
    @POST("recipes/{uuid}")
    Call<RetrofitUUIDResponse> createRecipe(@Path("uuid") String _uuid, @Query("token") String _token, @Body Recipe _Recipe);

    /**
     * Update the Recipe by the given Recipe.
     *
     * @param _uuid   the uuid of the affected Recipe.
     * @param _token  the auth token to identify the associated group.
     * @param _Recipe the Recipe with updates.
     * @return the uuid of the updated Recipe.
     */
    @PUT("recipes/{uuid}")
    Call<RetrofitUUIDResponse> updateRecipe(@Path("uuid") String _uuid, @Query("token") String _token, @Body Recipe _Recipe);

    /**
     * Delete the Recipe from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected Recipe.
     * @param _token the auth token to identify the associated group.
     * @return the uuid of the deleted element.
     */
    @DELETE("recipes/{uuid}")
    Call<RetrofitUUIDResponse> deleteRecipe(@Path("uuid") String _uuid, @Query("token") String _token);

    //endregion

    //region TaggedProduct

    /**
     * Get the taggedProducts since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @param _token           the auth token to identify the associated group.
     * @return the list of taggedProducts changed since this date.
     */
    @GET("taggedProducts")
    Call<List<TaggedProductResponse>> getTaggedProducts(@Query("token") String _token, @Query("changedSince") String _sinceTimeString);

    /**
     * Get a TaggedProduct by its id.
     *
     * @param _uuid  the uuid of the affected TaggedProduct.
     * @param _token the auth token to identify the associated group.
     * @return the associated TaggedProduct.
     */
    @GET("taggedProducts/{uuid}")
    Call<TaggedProductResponse> getTaggedProduct(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Create a TaggedProduct with the given id.
     *
     * @param _uuid          the uuid of the affected TaggedProduct.
     * @param _token         the auth token to identify the associated group.
     * @param _TaggedProduct the TaggedProduct to create.
     * @return the uuid of the created TaggedProduct.
     */
    @POST("taggedProducts/{uuid}")
    Call<RetrofitUUIDResponse> createTaggedProduct(@Path("uuid") String _uuid, @Query("token") String _token, @Body TaggedProduct _TaggedProduct);

    /**
     * Update the TaggedProduct by the given TaggedProduct.
     *
     * @param _uuid          the uuid of the affected TaggedProduct.
     * @param _token         the auth token to identify the associated group.
     * @param _TaggedProduct the TaggedProduct with updates.
     * @return the uuid of the updated TaggedProduct.
     */
    @PUT("taggedProducts/{uuid}")
    Call<RetrofitUUIDResponse> updateTaggedProduct(@Path("uuid") String _uuid, @Query("token") String _token, @Body TaggedProduct _TaggedProduct);

    /**
     * Delete the TaggedProduct from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected TaggedProduct.
     * @param _token the auth token to identify the associated group.
     * @return the uuid of the deleted element.
     */
    @DELETE("taggedProducts/{uuid}")
    Call<RetrofitUUIDResponse> deleteTaggedProduct(@Path("uuid") String _uuid, @Query("token") String _token);

    //endregion

    //region Tag

    /**
     * Get the tags since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @param _token           the auth token to identify the associated group.
     * @return the list of tags changed since this date.
     */
    @GET("tags")
    Call<List<TagResponse>> getTags(@Query("token") String _token, @Query("changedSince") String _sinceTimeString);

    /**
     * Get a Tag by its id.
     *
     * @param _uuid  the uuid of the affected Tag.
     * @param _token the auth token to identify the associated group.
     * @return the associated Tag.
     */
    @GET("tags/{uuid}")
    Call<TagResponse> getTag(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Create a Tag with the given id.
     *
     * @param _uuid  the uuid of the affected Tag.
     * @param _token the auth token to identify the associated group.
     * @param _Tag   the Tag to create.
     * @return the uuid of the created Tag.
     */
    @POST("tags/{uuid}")
    Call<RetrofitUUIDResponse> createTag(@Path("uuid") String _uuid, @Query("token") String _token, @Body Tag _Tag);

    /**
     * Update the Tag by the given Tag.
     *
     * @param _uuid  the uuid of the affected Tag.
     * @param _token the auth token to identify the associated group.
     * @param _Tag   the Tag with updates.
     * @return the uuid of the updated Tag.
     */
    @PUT("tags/{uuid}")
    Call<RetrofitUUIDResponse> updateTag(@Path("uuid") String _uuid, @Query("token") String _token, @Body Tag _Tag);

    /**
     * Delete the Tag from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected Tag.
     * @param _token the auth token to identify the associated group.
     * @return the uuid of the deleted element.
     */
    @DELETE("tags/{uuid}")
    Call<RetrofitUUIDResponse> deleteTag(@Path("uuid") String _uuid, @Query("token") String _token);

    //endregion

    //region ListEntry

    /**
     * Get the listEntries since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @param _token           the auth token to identify the associated group.
     * @return the list of listEntries changed since this date.
     */
    @GET("listEntries")
    Call<List<ListEntryResponse>> getListEntries(@Query("token") String _token, @Query("changedSince") String _sinceTimeString);

    /**
     * Get a ListEntry by its id.
     *
     * @param _uuid  the uuid of the affected ListEntry.
     * @param _token the auth token to identify the associated group.
     * @return the associated ListEntry.
     */
    @GET("listEntries/{uuid}")
    Call<ListEntryResponse> getListEntry(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Create a ListEntry with the given id.
     *
     * @param _uuid      the uuid of the affected ListEntry.
     * @param _token     the auth token to identify the associated group.
     * @param _ListEntry the ListEntry to create.
     * @return the uuid of the created ListEntry.
     */
    @POST("listEntries/{uuid}")
    Call<RetrofitUUIDResponse> createListEntry(@Path("uuid") String _uuid, @Query("token") String _token, @Body ListEntry _ListEntry);

    /**
     * Update the ListEntry by the given ListEntry.
     *
     * @param _uuid      the uuid of the affected ListEntry.
     * @param _token     the auth token to identify the associated group.
     * @param _ListEntry the ListEntry with updates.
     * @return the uuid of the updated ListEntry.
     */
    @PUT("listEntries/{uuid}")
    Call<RetrofitUUIDResponse> updateListEntry(@Path("uuid") String _uuid, @Query("token") String _token, @Body ListEntry _ListEntry);

    /**
     * Delete the ListEntry from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected ListEntry.
     * @param _token the auth token to identify the associated group.
     * @return the uuid of the deleted element.
     */
    @DELETE("listEntries/{uuid}")
    Call<RetrofitUUIDResponse> deleteListEntry(@Path("uuid") String _uuid, @Query("token") String _token);

    //endregion

    //region Unit

    /**
     * Get the units since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @param _token           the auth token to identify the associated group.
     * @return the list of units changed since this date.
     */
    @GET("units")
    Call<List<UnitResponse>> getUnits(@Query("token") String _token, @Query("changedSince") String _sinceTimeString);

    /**
     * Get a Unit by its id.
     *
     * @param _uuid  the uuid of the affected Unit.
     * @param _token the auth token to identify the associated group.
     * @return the associated Unit.
     */
    @GET("units/{uuid}")
    Call<UnitResponse> getUnit(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Create a Unit with the given id.
     *
     * @param _uuid  the uuid of the affected Unit.
     * @param _token the auth token to identify the associated group.
     * @param _Unit  the Unit to create.
     * @return the uuid of the created Unit.
     */
    @POST("units/{uuid}")
    Call<RetrofitUUIDResponse> createUnit(@Path("uuid") String _uuid, @Query("token") String _token, @Body Unit _Unit);

    /**
     * Update the Unit by the given Unit.
     *
     * @param _uuid  the uuid of the affected Unit.
     * @param _token the auth token to identify the associated group.
     * @param _Unit  the Unit with updates.
     * @return the uuid of the updated Unit.
     */
    @PUT("units/{uuid}")
    Call<RetrofitUUIDResponse> updateUnit(@Path("uuid") String _uuid, @Query("token") String _token, @Body Unit _Unit);

    /**
     * Delete the Unit from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected Unit.
     * @param _token the auth token to identify the associated group.
     * @return the uuid of the deleted element.
     */
    @DELETE("units/{uuid}")
    Call<RetrofitUUIDResponse> deleteUnit(@Path("uuid") String _uuid, @Query("token") String _token);

    //endregion

    //region Ingredient

    /**
     * Get the ingredients since the given time.
     *
     * @param _sinceTimeString in ISO8601 format.
     * @param _token           the auth token to identify the associated group.
     * @return the list of ingredients changed since this date.
     */
    @GET("ingredients")
    Call<List<IngredientResponse>> getIngredients(@Query("token") String _token, @Query("changedSince") String _sinceTimeString);

    /**
     * Get a Ingredient by its id.
     *
     * @param _uuid  the uuid of the affected Ingredient.
     * @param _token the auth token to identify the associated group.
     * @return the associated Ingredient.
     */
    @GET("ingredients/{uuid}")
    Call<IngredientResponse> getIngredient(@Path("uuid") String _uuid, @Query("token") String _token);

    /**
     * Create a Ingredient with the given id.
     *
     * @param _uuid       the uuid of the affected Ingredient.
     * @param _token      the auth token to identify the associated group.
     * @param _Ingredient the Ingredient to create.
     * @return the uuid of the created Ingredient.
     */
    @POST("ingredients/{uuid}")
    Call<RetrofitUUIDResponse> createIngredient(@Path("uuid") String _uuid, @Query("token") String _token, @Body Ingredient _Ingredient);

    /**
     * Update the Ingredient by the given Ingredient.
     *
     * @param _uuid       the uuid of the affected Ingredient.
     * @param _token      the auth token to identify the associated group.
     * @param _Ingredient the Ingredient with updates.
     * @return the uuid of the updated Ingredient.
     */
    @PUT("ingredients/{uuid}")
    Call<RetrofitUUIDResponse> updateIngredient(@Path("uuid") String _uuid, @Query("token") String _token, @Body Ingredient _Ingredient);

    /**
     * Delete the Ingredient from the remote server with the given id.
     *
     * @param _uuid  the uuid of the affected Ingredient.
     * @param _token the auth token to identify the associated group.
     * @return the uuid of the deleted element.
     */
    @DELETE("ingredients/{uuid}")
    Call<RetrofitUUIDResponse> deleteIngredient(@Path("uuid") String _uuid, @Query("token") String _token);

    //endregion

}
