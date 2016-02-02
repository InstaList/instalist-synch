package org.noorganization.instalistsynch.controller.network.impl;

import org.noorganization.instalistsynch.controller.network.IGroupManager;

/**
 * Factory for controllers to access network.
 * Created by tinos_000 on 02.02.2016.
 */
public class NetworkControllerFactory {

    /**
     * Get an instance of the V1 Groupmanager implementation.
     * @return the instance of groupmanager.
     */
    public static IGroupManager getGroupManager(){
        return V1GroupManager.getInstance();
    }
}
