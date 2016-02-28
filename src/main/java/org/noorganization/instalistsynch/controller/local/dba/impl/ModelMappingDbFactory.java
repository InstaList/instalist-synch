package org.noorganization.instalistsynch.controller.local.dba.impl;

import org.noorganization.instalist.enums.eModelType;
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

        if (mModelDbMap.get(eModelType.LIST.ordinal()) == null) {
            try {
                mModelDbMap.put(eModelType.LIST.ordinal(), new SqliteMappingDbController(eModelMappingTableNames.LIST,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(eModelType.LIST.ordinal());
    }

    /**
     * Get the category mapping controller.
     *
     * @return the category mapping controller.
     */
    public IModelMappingDbController getSqliteCategoryMappingDbController() {
        if (mModelDbMap.get(eModelType.CATEGORY.ordinal()) == null) {
            try {
                mModelDbMap.put(eModelType.CATEGORY.ordinal(), new SqliteMappingDbController(eModelMappingTableNames.CATEGORY,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(eModelType.CATEGORY.ordinal());
    }

    /**
     * Get the product mapping controller.
     *
     * @return the producht mappig controller.
     */
    public IModelMappingDbController getSqliteProductMappingController() {
        if (mModelDbMap.get(eModelType.PRODUCT.ordinal()) == null) {
            try {
                mModelDbMap.put(eModelType.PRODUCT.ordinal(), new SqliteMappingDbController(eModelMappingTableNames.PRODUCT,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(eModelType.PRODUCT.ordinal());
    }

    /**
     * Get the unit mapping controller.
     *
     * @return the unit mapping controller.
     */
    public IModelMappingDbController getSqliteUnitMappingController() {
        if (mModelDbMap.get(eModelType.UNIT.ordinal()) == null) {
            try {
                mModelDbMap.put(eModelType.UNIT.ordinal(), new SqliteMappingDbController(eModelMappingTableNames.UNIT,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(eModelType.UNIT.ordinal());
    }

    /***
     * Get the mapping controller for the tags.
     *
     * @return the tag mapping controller.
     */
    public IModelMappingDbController getSqliteTagMappingController() {
        return getMappingController(eModelType.TAG, eModelMappingTableNames.TAG);
    }

    private IModelMappingDbController getMappingController(eModelType _modelType, eModelMappingTableNames _modelTableNames) {
        if (mModelDbMap.get(_modelType.ordinal()) == null) {
            try {
                mModelDbMap.put(_modelType.ordinal(), new SqliteMappingDbController(_modelTableNames,
                        GlobalObjects.getInstance().getApplicationContext()));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(_modelType.ordinal());
    }

    /**
     * Get the recipe mapping controller.
     * @return the recipe mapping controller.
     */
    public IModelMappingDbController getSqliteRecipeMappingController() {
        return getMappingController(eModelType.RECIPE, eModelMappingTableNames.RECIPE);
    }

    /**
     * Get the ingredient mapping controller.
     * @return the ingredient mapping controller.
     */
    public IModelMappingDbController getSqliteIngredientMappingDbController() {
        return getMappingController(eModelType.INGREDIENT, eModelMappingTableNames.INGREDIENT);
    }

    /**
     * Get the list entry mapping controller.
     * @return the list entry mapping controller.
     */
    public IModelMappingDbController getSqliteListEntryMappingController() {
        return getMappingController(eModelType.LIST_ENTRY, eModelMappingTableNames.LIST_ENTRY);
    }
}
