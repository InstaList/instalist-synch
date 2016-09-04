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

package org.noorganization.instalistsynch.controller.local.dba.impl;

import org.noorganization.instalist.types.ModelType;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.exception.SqliteMappingDbControllerException;
import org.noorganization.instalistsynch.model.eModelMappingTableNames;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping Factory for mapping models.
 * Created by tinos_000 on 31.01.2016.
 */
public class ModelMappingDbFactory {

    private static ModelMappingDbFactory sInstance;
    private Map<Integer, Object> mModelDbMap;

    public static ModelMappingDbFactory getInstance() {
        if (sInstance == null)
            sInstance = new ModelMappingDbFactory();

        return sInstance;
    }

    private ModelMappingDbFactory() {
        mModelDbMap = new HashMap<>(8);
    }

    /**
     * Get the {@link SqliteMappingDbController}.
     *
     * @return the controller to handle ShoppingListMapping Db interactions. Or null if table does not exist!
     */
    public IModelMappingDbController getSqliteShoppingListMappingDbController() {

        if (mModelDbMap.get(ModelType.LIST) == null) {
            try {
                mModelDbMap.put(ModelType.LIST, new SqliteMappingDbController(eModelMappingTableNames.LIST,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(ModelType.LIST);
    }

    /**
     * Get the category mapping controller.
     *
     * @return the category mapping controller.
     */
    public IModelMappingDbController getSqliteCategoryMappingDbController() {
        if (mModelDbMap.get(ModelType.CATEGORY) == null) {
            try {
                mModelDbMap.put(ModelType.CATEGORY, new SqliteMappingDbController(eModelMappingTableNames.CATEGORY,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(ModelType.CATEGORY);
    }

    /**
     * Get the product mapping controller.
     *
     * @return the producht mappig controller.
     */
    public IModelMappingDbController getSqliteProductMappingController() {
        if (mModelDbMap.get(ModelType.PRODUCT) == null) {
            try {
                mModelDbMap.put(ModelType.PRODUCT, new SqliteMappingDbController(eModelMappingTableNames.PRODUCT,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(ModelType.PRODUCT);
    }

    /**
     * Get the unit mapping controller.
     *
     * @return the unit mapping controller.
     */
    public IModelMappingDbController getSqliteUnitMappingController() {
        if (mModelDbMap.get(ModelType.UNIT) == null) {
            try {
                mModelDbMap.put(ModelType.UNIT, new SqliteMappingDbController(eModelMappingTableNames.UNIT,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(ModelType.UNIT);
    }

    /***
     * Get the mapping controller for the tags.
     *
     * @return the tag mapping controller.
     */
    public IModelMappingDbController getSqliteTagMappingController() {
        return getMappingController(ModelType.TAG, eModelMappingTableNames.TAG);
    }

    private IModelMappingDbController getMappingController(@ModelType.Model int _modelType, eModelMappingTableNames _modelTableNames) {
        if (mModelDbMap.get(_modelType) == null) {
            try {
                mModelDbMap.put(_modelType, new SqliteMappingDbController(_modelTableNames,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(_modelType);
    }

    /**
     * Get the recipe mapping controller.
     *
     * @return the recipe mapping controller.
     */
    public IModelMappingDbController getSqliteRecipeMappingController() {
        return getMappingController(ModelType.RECIPE, eModelMappingTableNames.RECIPE);
    }

    /**
     * Get the ingredient mapping controller.
     *
     * @return the ingredient mapping controller.
     */
    public IModelMappingDbController getSqliteIngredientMappingDbController() {
        return getMappingController(ModelType.INGREDIENT, eModelMappingTableNames.INGREDIENT);
    }

    /**
     * Get the list entry mapping controller.
     *
     * @return the list entry mapping controller.
     */
    public IModelMappingDbController getSqliteListEntryMappingController() {
        return getMappingController(ModelType.LIST_ENTRY, eModelMappingTableNames.LIST_ENTRY);
    }
}
