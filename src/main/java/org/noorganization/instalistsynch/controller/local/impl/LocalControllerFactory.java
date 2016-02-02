package org.noorganization.instalistsynch.controller.local.impl;

import android.content.Context;

import org.noorganization.instalistsynch.controller.local.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.IGroupMemberDbController;

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

    /**
     * Get the {@link IGroupAuthAccessDbController} implementation for Sqlitedatabase.
     *
     * @param _context the context of the app.
     * @return the sqlite DbAuthAccessDbController.
     */
    public static IGroupAuthAccessDbController getSqliteAuthAccessController(Context _context) {
        return SqliteGroupAuthAccessDbController.getInstance(_context);
    }

    /**
     * Get the sqlite implementation of the groupmemberDbController.
     *
     * @param _context the context of the app.
     * @return the requested controller instance.
     */
    public static IGroupMemberDbController getGroupMemberDbController(Context _context) {
        return GroupMemberDbController.getInstance(_context);
    }
}
