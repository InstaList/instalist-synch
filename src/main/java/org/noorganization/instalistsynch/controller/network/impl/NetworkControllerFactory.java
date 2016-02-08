package org.noorganization.instalistsynch.controller.network.impl;

import org.noorganization.instalistsynch.controller.network.IAuthNetworkController;
import org.noorganization.instalistsynch.controller.network.IGroupNetworkController;
import org.noorganization.instalistsynch.controller.network.ISessionController;

/**
 * Factory for controllers to access network.
 * Created by tinos_000 on 02.02.2016.
 */
public class NetworkControllerFactory {

    /**
     * Get an instance of the V1GroupNetworkController implementation.
     *
     * @return the instance of V1GroupNetworkController.
     */
    public static IGroupNetworkController getGroupController() {
        return V1GroupNetworkController.getInstance();
    }

    /**
     * Get the default session controller.
     *
     * @return the instance of InMemorySessionManager.
     */
    public static ISessionController getSessionController() {
        return InMemorySessionController.getInstance();
    }

    /**
     * Get the AuthNetworkController instance.
     *
     * @return the AuthNetworkController instance.
     */
    public static IAuthNetworkController getAuthNetworkController() {
        return AuthNetworkController.getInstance();
    }
}
