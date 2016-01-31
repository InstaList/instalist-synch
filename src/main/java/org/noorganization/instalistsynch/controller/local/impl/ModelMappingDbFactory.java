package org.noorganization.instalistsynch.controller.local.impl;

import org.noorganization.instalistsynch.controller.local.IModelMappingDbController;

/**
 * Mapping Factory for mapping models.
 * Created by tinos_000 on 31.01.2016.
 */
public class ModelMappingDbFactory {

    /**
     * Get the {@link SqliteShoppingListMappingDbController}.
     * @return the controller to handle ShoppingListMapping Db interactions.
     */
    public static IModelMappingDbController getSqliteShoppingListMappingDbController(){
        return SqliteShoppingListMappingDbController.getInstance();
    }

}
