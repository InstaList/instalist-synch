package org.noorganization.instalistsynch.controller.local.impl;

import android.content.Context;

import org.noorganization.instalistsynch.controller.local.IGroupAuthDbController;

/**
 * Get all controllers related to the synching process.
 * Created by tinos_000 on 29.01.2016.
 */
public class LocalControllerFactory {

    /**
     * Get the instance of the default {@link IGroupAuthDbController} implementation {@link GroupAuthDbController}.
     *
     * @param _context the context of the app.
     * @return the default GroupAuthController.
     */
    public static IGroupAuthDbController getDefaultAuthController(Context _context) {
        return GroupAuthDbController.getInstance(_context);
    }
}
