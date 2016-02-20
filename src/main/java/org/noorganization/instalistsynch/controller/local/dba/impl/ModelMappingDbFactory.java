package org.noorganization.instalistsynch.controller.local.dba.impl;

import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.exception.SqliteMappingDbControllerException;
import org.noorganization.instalistsynch.model.network.eModelMappingTableNames;
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
}
