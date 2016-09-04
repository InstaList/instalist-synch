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

package org.noorganization.instalistsynch.controller.network.model;

import org.noorganization.instalist.comm.message.CategoryInfo;
import org.noorganization.instalist.comm.message.EntryInfo;
import org.noorganization.instalist.comm.message.IngredientInfo;
import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.comm.message.ProductInfo;
import org.noorganization.instalist.comm.message.RecipeInfo;
import org.noorganization.instalist.comm.message.TagInfo;
import org.noorganization.instalist.comm.message.TaggedProductInfo;
import org.noorganization.instalist.comm.message.UnitInfo;
import org.noorganization.instalist.types.ModelType;
import org.noorganization.instalistsynch.controller.network.model.generic.ModelNetworkController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory to get all Synchronization Controller of all defined models.
 * Created by tinos_000 on 30.01.2016.
 */
public class RemoteModelAccessControllerFactory {

    private static RemoteModelAccessControllerFactory sInstance;
    private Map<Integer, INetworkController> mMapping;

    /**
     * Get the instance of the factory.
     *
     * @return the instance of the factory.
     */
    public static RemoteModelAccessControllerFactory getInstance() {
        if (sInstance == null) {
            sInstance = new RemoteModelAccessControllerFactory();
        }
        return sInstance;
    }

    private RemoteModelAccessControllerFactory() {
        mMapping = new ConcurrentHashMap<>(ModelType.ALL);
        init();
    }

    /**
     * Initializes the mapping map for the network interaction.
     */
    public void init() {
        mMapping.put(ModelType.CATEGORY,
                new ModelNetworkController<CategoryInfo>(ModelType.CATEGORY));
        mMapping.put(ModelType.LIST,
                new ModelNetworkController<ListInfo>(ModelType.LIST));
        mMapping.put(ModelType.LIST_ENTRY,
                new ModelNetworkController<EntryInfo>(ModelType.LIST_ENTRY));
        mMapping.put(ModelType.INGREDIENT,
                new ModelNetworkController<IngredientInfo>(ModelType.INGREDIENT));
        mMapping.put(ModelType.PRODUCT,
                new ModelNetworkController<ProductInfo>(ModelType.PRODUCT));
        mMapping.put(ModelType.RECIPE,
                new ModelNetworkController<RecipeInfo>(ModelType.RECIPE));
        mMapping.put(ModelType.TAG,
                new ModelNetworkController<TagInfo>(ModelType.TAG));
        mMapping.put(ModelType.TAGGED_PRODUCT,
                new ModelNetworkController<TaggedProductInfo>(ModelType.TAGGED_PRODUCT));
        mMapping.put(ModelType.UNIT,
                new ModelNetworkController<UnitInfo>(ModelType.UNIT));
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the List networkcontroller interface.
     */
    public INetworkController<ListInfo> getListNetworkController() {
        return mMapping.get(ModelType.LIST);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the category networkcontroller interface.
     */
    public INetworkController<CategoryInfo> getCategoryNetworkController() {
        return mMapping.get(ModelType.CATEGORY);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the tag networkcontroller interface.
     */
    public INetworkController<TagInfo> getTagNetworkController() {
        return mMapping.get(ModelType.TAG);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the recipe networkcontroller interface.
     */
    public INetworkController<RecipeInfo> getRecipeNetworkController() {
        return mMapping.get(ModelType.RECIPE);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the ingredient networkcontroller interface.
     */
    public INetworkController<IngredientInfo> getIngredientNetworkController() {
        return mMapping.get(ModelType.INGREDIENT);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the ingredient networkcontroller interface.
     */
    public INetworkController<EntryInfo> getListEntryNetworkController() {
        return mMapping.get(ModelType.LIST_ENTRY);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the product networkcontroller interface.
     */
    public INetworkController<ProductInfo> getProductNetworkController() {
        return mMapping.get(ModelType.PRODUCT);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the taggedproduct networkcontroller interface.
     */
    public INetworkController<TaggedProductInfo> getTaggedProductNetworkController() {
        return mMapping.get(ModelType.TAGGED_PRODUCT);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the unit networkcontroller interface.
     */
    public INetworkController<UnitInfo> getUnitNetworkController() {
        return mMapping.get(ModelType.UNIT);
    }
}
