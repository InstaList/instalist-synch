package org.noorganization.instalistsynch.controller.local.dba;

import android.content.Context;

import org.noorganization.instalistsynch.controller.local.dba.impl.ClientLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.GroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.GroupMemberDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.SqliteGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.TaskErrorLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.TempGroupAccessTokenDbController;

/**
 * Get all controllers that manage the access to database.
 * Created by tinos_000 on 29.01.2016.
 */
public class LocalSqliteDbControllerFactory {

    /**
     * Get the sqlite implementation of the {@link IGroupAuthDbController} interface.
     *
     * @param _context the context of the app.
     * @return the default GroupAuthController.
     */
    public static IGroupAuthDbController getGroupAuthDbController(Context _context) {
        return GroupAuthDbController.getInstance(_context);
    }

    /**
     * Get the sqlite implementation of the {@link IGroupAuthAccessDbController} interface.
     *
     * @param _context the context of the app.
     * @return the sqlite DbAuthAccessDbController.
     */
    public static IGroupAuthAccessDbController getAuthAccessDbController(Context _context) {
        return SqliteGroupAuthAccessDbController.getInstance(_context);
    }

    /**
     * Get the sqlite implementation of the {@link IGroupMemberDbController} interface.
     *
     * @param _context the context of the app.
     * @return the requested controller instance.
     */
    public static IGroupMemberDbController getGroupMemberDbController(Context _context) {
        return GroupMemberDbController.getInstance(_context);
    }

    /**
     * Get the sqlite implementation of the {@link ITempGroupAccessTokenDbController} interface.
     *
     * @param _context the context of the app.
     * @return the requested controller instance.
     */
    public static ITempGroupAccessTokenDbController getTempGroupAccessTokenDbController(
            Context _context) {
        return TempGroupAccessTokenDbController.getInstance(_context);
    }

    /**
     * Get the sqlite implementation of the {@link ITaskErrorLogDbController} interface.
     *
     * @param _context the context of the app.
     * @return the requested controller instance.
     */
    public static ITaskErrorLogDbController getTaskErrorLogDbController(Context _context) {
        return TaskErrorLogDbController.getInstance(_context);
    }

    /**
     * Get the sqlite implementation of the {@link ITaskErrorLogDbController} interface.
     *
     * @param _context the context of the app.
     * @return the requested controller instance.
     */
    public static IClientLogDbController getClientLogController(Context _context) {
        return ClientLogDbController.getInstance(_context);
    }


}
