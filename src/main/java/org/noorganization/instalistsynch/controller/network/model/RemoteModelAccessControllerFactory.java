package org.noorganization.instalistsynch.controller.network.model;

import org.noorganization.instalistsynch.controller.network.model.IListNetworkController;
import org.noorganization.instalistsynch.controller.network.model.impl.ListNetworkController;

/**
 * Factory to get all Synchronization Controller of all defined models.
 * Created by tinos_000 on 30.01.2016.
 */
public class RemoteModelAccessControllerFactory {

    /**
     * Get the instance of the {@link ListNetworkController}.
     * @return the SynchController of the ShoppingList.
     */
    public static IListNetworkController getListSynchController(){
        return ListNetworkController.getInstance();
    }
}
