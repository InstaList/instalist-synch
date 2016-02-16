package org.noorganization.instalistsynch.controller.local.dba;

import android.database.Cursor;

import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;

import java.util.Date;

/**
 * Used to log the synchronisation process to a database.
 * Created by tinos_000 on 10.02.2016.
 */
public interface ISynchLogDbController {

    /**
     * Insert into the log.
     * @param _groupId the id of the group.
     * @param _modelType the type of the model.
     * @param _actionType the type of action.
     * @param _uuidLocal the local uuid.
     * @param _uuidRemote the remote uuid.
     * @return true if insertion went good false if not.
     */
    boolean insertAction(int _groupId, eModelType _modelType, eActionType _actionType, String _uuidLocal, String _uuidRemote);

    /**
     * Clear the log before the specified time.
     * @param _time the time until the entries should be removed.
     */
    boolean clearLogData(Date _time);

    /**
     * Get the logged data.
     *
     * @param _groupId    the log of the specified group id.
     * @param _modelType  the log of a specified model.
     * @param _actionType the type of action of a specified action.
     * @return the requestet log or an empty cursor.
     */
    Cursor getLog(int _groupId, eModelType _modelType, eActionType _actionType);

}
