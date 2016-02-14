package org.noorganization.instalistsynch.controller.local.dba.impl;

import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.exception.SqliteMappingDbControllerException;
import org.noorganization.instalistsynch.model.network.ModelMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * Mapping Factory for mapping models.
 * Created by tinos_000 on 31.01.2016.
 */
public class ModelMappingDbFactory {

    private static ModelMappingDbFactory sInstance;
    private Map<Integer, Object> mModelDbMap;

    private final static int SHOPPING_LIST = 0;

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
        if (mModelDbMap.get(SHOPPING_LIST) == null) {
            try {
                mModelDbMap.put(SHOPPING_LIST, new SqliteMappingDbController(ModelMapping.SHOPPING_LIST_MAPPING_TABLE_NAME));
            } catch (SqliteMappingDbControllerException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (IModelMappingDbController) mModelDbMap.get(SHOPPING_LIST);
    }

    public IModelMappingDbController getSqliteCategoryMappingDbController() {
        //TODO implement
        return null;
    }
}
