package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.controller.network.IAuthNetworkController;
import org.noorganization.instalistsynch.controller.network.IGroupNetworkController;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.AuthNetworkController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.impl.V1GroupNetworkController;

/**
 * Factory for controllers to access network controllers that are related to
 * authorization and user management.
 * Created by tinos_000 on 02.02.2016.
 */
public class AuthNetworkControllerFactory {

    /**
     * Get an instance of the V1GroupNetworkController implementation.
     *
     * @return the instance of V1GroupNetworkController.
     */
    public static synchronized IGroupNetworkController getGroupController() {
        return V1GroupNetworkController.getInstance();
    }

    /**
     * Get the default session controller.
     *
     * @return the instance of InMemorySessionManager.
     */
    public static synchronized ISessionController getSessionController() {
        return InMemorySessionController.getInstance();
    }

    /**
     * Get the AuthNetworkController instance.
     *
     * @return the AuthNetworkController instance.
     */
    public static synchronized IAuthNetworkController getAuthNetworkController() {
        return AuthNetworkController.getInstance();
    }
}
