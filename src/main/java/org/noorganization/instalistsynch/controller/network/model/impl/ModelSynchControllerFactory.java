package org.noorganization.instalistsynch.controller.network.model.impl;

import org.noorganization.instalistsynch.controller.network.model.IListNetworkController;
import org.noorganization.instalistsynch.controller.network.model.IModelSynchController;

/**
 * Factory to get all Synchronization Controller of all defined models.
 * Created by tinos_000 on 30.01.2016.
 */
public class ModelSynchControllerFactory {

    /**
     * Get the instance of the {@link ShoppingListSynchController}.
     * @return the SynchController of the ShoppingList.
     */
    public static IListNetworkController getShoppingListSynchController(){
        return ListNetworkController.getInstance();
    }
}
