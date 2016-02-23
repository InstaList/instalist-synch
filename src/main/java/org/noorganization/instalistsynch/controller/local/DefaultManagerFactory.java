package org.noorganization.instalistsynch.controller.local;

import org.noorganization.instalistsynch.controller.local.IAuthManagerController;
import org.noorganization.instalistsynch.controller.local.IGroupManagerController;
import org.noorganization.instalistsynch.controller.local.impl.DefaultAuthManagerController;
import org.noorganization.instalistsynch.controller.local.impl.DefaultGroupManagerController;

/**
 * Get the default manager instances.
 * Created by tinos_000 on 10.02.2016.
 */
public class DefaultManagerFactory {

    /**
     * Get the DefaultAuthManagerController instance.
     *
     * @return the DefaultAuthManagerController instance.
     */
    public static IAuthManagerController getAuthManagerController() {
        return DefaultAuthManagerController.getInstance();
    }

    /**
     * Get the DefaultGroupManagerController instance.
     *
     * @return the DefaultGroupManagerController instance.
     */
    public static IGroupManagerController getGroupManagerController() {
        return DefaultGroupManagerController.getInstance();
    }
}