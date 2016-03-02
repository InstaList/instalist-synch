/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.network.model.generic;

import org.noorganization.instalist.comm.message.CategoryInfo;
import org.noorganization.instalist.comm.message.EntryInfo;
import org.noorganization.instalist.comm.message.IngredientInfo;
import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.comm.message.ProductInfo;
import org.noorganization.instalist.comm.message.RecipeInfo;
import org.noorganization.instalist.comm.message.TagInfo;
import org.noorganization.instalist.comm.message.TaggedProductInfo;
import org.noorganization.instalist.comm.message.UnitInfo;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;
import org.noorganization.instalistsynch.controller.handler.AuthorizedCallbackHandler;
import org.noorganization.instalistsynch.controller.handler.AuthorizedInsertCallbackHandler;
import org.noorganization.instalistsynch.controller.network.api.authorized.ICategoryApiService;
import org.noorganization.instalistsynch.controller.network.api.authorized.IIngredientApiService;
import org.noorganization.instalistsynch.controller.network.api.authorized.IListApiService;
import org.noorganization.instalistsynch.controller.network.api.authorized.IListEntryApiService;
import org.noorganization.instalistsynch.controller.network.api.authorized.IProductApiService;
import org.noorganization.instalistsynch.controller.network.api.authorized.IRecipeApiService;
import org.noorganization.instalistsynch.controller.network.api.authorized.ITagApiService;
import org.noorganization.instalistsynch.controller.network.api.authorized.ITaggedProductApiService;
import org.noorganization.instalistsynch.controller.network.api.authorized.IUnitApiService;
import org.noorganization.instalistsynch.controller.network.model.INetworkController;
import org.noorganization.instalistsynch.utils.ApiUtils;

import java.util.List;

import retrofit2.Call;

/**
 * The generic implementation for all models to be synched with the server.
 * Generic T is the response and request model
 * Generics for extended base interface is not possible with retrofit 2, this is a design choice!
 * info : (Because of that this soulution uses the modeltype to specify all network controllers)
 * Created by Desnoo on 23.02.2016.
 */
public class ModelNetworkController<T> implements INetworkController<T> {
    /*  private static ListNetworkController sInstance;

      public static ListNetworkController getInstance() {
          if (sInstance == null)
              sInstance = new ListNetworkController();
          return sInstance;
      }

      private ListNetworkController(){

      }
  */
    private final eModelType mModelType;

    /**
     * The generic class;
     *
     * @param _modelType the type of the model.
     */
    public ModelNetworkController(eModelType _modelType) {
        mModelType = _modelType;
    }

    @Override
    public void getList(IAuthorizedCallbackCompleted<List<T>> _callback, int _groupId, String _time,
            String _authToken) {
        Call call;

        switch (mModelType) {
            case TAGGED_PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITaggedProductApiService.class, _authToken)
                        .getList(_groupId, _time);
                break;
            case LIST:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListApiService.class, _authToken)
                        .getList(_groupId, _time);
                break;
            case TAG:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITagApiService.class, _authToken)
                        .getList(_groupId, _time);
                break;
            case RECIPE:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IRecipeApiService.class, _authToken)
                        .getList(_groupId, _time);
                break;
            case PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IProductApiService.class, _authToken)
                        .getList(_groupId, _time);
                break;
            case CATEGORY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ICategoryApiService.class, _authToken)
                        .getList(_groupId, _time);
                break;
            case INGREDIENT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IIngredientApiService.class, _authToken)
                        .getList(_groupId, _time);
                break;
            case LIST_ENTRY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListEntryApiService.class, _authToken)
                        .getList(_groupId, _time);
                break;
            case UNIT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IUnitApiService.class, _authToken)
                        .getList(_groupId, _time);
                break;
            default:
                return;
        }
        call.enqueue(new AuthorizedCallbackHandler<List<T>>(_groupId, _callback, call));

    }

    @Override
    public void getItem(IAuthorizedCallbackCompleted<T> _callback, int _groupId, String _uuid,
            String _authToken) {
        Call call;

        switch (mModelType) {
            case TAGGED_PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITaggedProductApiService.class, _authToken)
                        .getItem(_groupId, _uuid);
                break;
            case LIST:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListApiService.class, _authToken)
                        .getItem(_groupId, _uuid);
                break;
            case TAG:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITagApiService.class, _authToken)
                        .getItem(_groupId, _uuid);
                break;
            case RECIPE:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IRecipeApiService.class, _authToken)
                        .getItem(_groupId, _uuid);
                break;
            case PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IProductApiService.class, _authToken)
                        .getItem(_groupId, _uuid);
                break;
            case CATEGORY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ICategoryApiService.class, _authToken)
                        .getItem(_groupId, _uuid);
                break;
            case INGREDIENT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IIngredientApiService.class, _authToken)
                        .getItem(_groupId, _uuid);
                break;
            case LIST_ENTRY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListEntryApiService.class, _authToken)
                        .getItem(_groupId, _uuid);
                break;
            case UNIT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IUnitApiService.class, _authToken)
                        .getItem(_groupId, _uuid);
                break;
            default:
                return;
        }
        call.enqueue(new AuthorizedCallbackHandler<T>(_groupId, _callback, call));
    }

    @Override
    public void createItem(IAuthorizedInsertCallbackCompleted<Void> _callback, int _groupId,
            T _item, String _authToken) {
        Call<Void> call;
        switch (mModelType) {
            case TAGGED_PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITaggedProductApiService.class, _authToken)
                        .createItem(_groupId, (TaggedProductInfo) _item);
                break;
            case LIST:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListApiService.class, _authToken)
                        .createItem(_groupId, (ListInfo) _item);
                break;
            case TAG:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITagApiService.class, _authToken)
                        .createItem(_groupId, (TagInfo) _item);
                break;
            case RECIPE:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IRecipeApiService.class, _authToken)
                        .createItem(_groupId, (RecipeInfo) _item);
                break;
            case PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IProductApiService.class, _authToken)
                        .createItem(_groupId, (ProductInfo) _item);
                break;
            case CATEGORY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ICategoryApiService.class, _authToken)
                        .createItem(_groupId, (CategoryInfo) _item);
                break;
            case INGREDIENT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IIngredientApiService.class, _authToken)
                        .createItem(_groupId, (IngredientInfo) _item);
                break;
            case LIST_ENTRY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListEntryApiService.class, _authToken)
                        .createItem(_groupId, (EntryInfo) _item);
                break;
            case UNIT:
                call = ApiUtils.getInstance().getAuthorizedApiService(IUnitApiService.class,
                        _authToken)
                        .createItem(_groupId, (UnitInfo) _item);
                break;
            default:
                return;
        }

        call.enqueue(new AuthorizedInsertCallbackHandler<Void>(_groupId, _callback, call));
    }

    @Override
    public void updateItem(IAuthorizedCallbackCompleted<Void> _callback, int _groupId, String _uuid,
            T _item, String _authToken) {
        Call<Void> call;
        switch (mModelType) {
            case TAGGED_PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITaggedProductApiService.class, _authToken)
                        .updateItem(_groupId, _uuid, (TaggedProductInfo) _item);
                break;
            case LIST:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListApiService.class, _authToken)
                        .updateItem(_groupId, _uuid, (ListInfo) _item);
                break;
            case TAG:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITagApiService.class, _authToken)
                        .updateItem(_groupId, _uuid, (TagInfo) _item);
                break;
            case RECIPE:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IRecipeApiService.class, _authToken)
                        .updateItem(_groupId, _uuid, (RecipeInfo) _item);
                break;
            case PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IProductApiService.class, _authToken)
                        .updateItem(_groupId, _uuid, (ProductInfo) _item);
                break;
            case CATEGORY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ICategoryApiService.class, _authToken)
                        .updateItem(_groupId, _uuid, (CategoryInfo) _item);
                break;
            case INGREDIENT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IIngredientApiService.class, _authToken)
                        .updateItem(_groupId, _uuid, (IngredientInfo) _item);
                break;
            case LIST_ENTRY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListEntryApiService.class, _authToken)
                        .updateItem(_groupId, _uuid, (EntryInfo) _item);
                break;
            case UNIT:
                call = ApiUtils.getInstance().getAuthorizedApiService(IUnitApiService.class,
                        _authToken)
                        .updateItem(_groupId, _uuid, (UnitInfo) _item);
                break;
            default:
                return;
        }

        call.enqueue(new AuthorizedCallbackHandler<Void>(_groupId, _callback, call));
    }

    @Override
    public void deleteItem(IAuthorizedCallbackCompleted<Void> _callback, int _groupId, String _uuid,
            String _authToken) {

        Call<Void> call;
        switch (mModelType) {
            case TAGGED_PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITaggedProductApiService.class, _authToken)
                        .deleteItem(_groupId, _uuid);
                break;
            case LIST:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListApiService.class, _authToken)
                        .deleteItem(_groupId, _uuid);
                break;
            case TAG:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ITagApiService.class, _authToken)
                        .deleteItem(_groupId, _uuid);
                break;
            case RECIPE:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IRecipeApiService.class, _authToken)
                        .deleteItem(_groupId, _uuid);
                break;
            case PRODUCT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IProductApiService.class, _authToken)
                        .deleteItem(_groupId, _uuid);
                break;
            case CATEGORY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(ICategoryApiService.class, _authToken)
                        .deleteItem(_groupId, _uuid);
                break;
            case INGREDIENT:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IIngredientApiService.class, _authToken)
                        .deleteItem(_groupId, _uuid);
                break;
            case LIST_ENTRY:
                call = ApiUtils.getInstance()
                        .getAuthorizedApiService(IListEntryApiService.class, _authToken)
                        .deleteItem(_groupId, _uuid);
                break;
            case UNIT:
                call = ApiUtils.getInstance().getAuthorizedApiService(IUnitApiService.class,
                        _authToken)
                        .deleteItem(_groupId, _uuid);
                break;
            default:
                return;
        }

        call.enqueue(new AuthorizedCallbackHandler<Void>(_groupId, _callback, call));
    }
}
