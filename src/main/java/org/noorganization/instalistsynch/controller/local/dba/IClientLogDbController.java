package org.noorganization.instalistsynch.controller.local.dba;

import android.database.Cursor;

import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.LogInfo;

import java.util.Date;
import java.util.List;

/**
 * Interface to access the log db.
 * Created by Desnoo on 16.02.2016.
 */
public interface IClientLogDbController {

    /**
     * Get all logs.
     *
     * @return a cursor pointing to the logs.
     */
    Cursor getLogs();

    /**
     * Get all logs since a given time.
     *
     * @param _date      the date since when to check changes.
     * @param _modelType the type of the model.
     * @return a cursor pointing to the logs that are sorted ascending.
     */
    Cursor getLogsSince(String _date, eModelType _modelType);

    /**
     * Get the LogInfo entries since a given time for a specified uuid of a element with the type of the specified model.
     *
     * @param _uuid       the uuid of the client side model.
     * @param _modelType  the type of the model on the client side.
     * @param _actionType the type of the action.
     * @param _date       the string of the date.
     * @return a list with matching entries.
     */
    List<LogInfo> getElementByUuid(String _uuid, eActionType _actionType, eModelType _modelType,
            String _date);


    /**
     * Get the least recent update time of the client uuid.
     *
     * @param _clientUuid the uuid of the item to find.
     * @return the last update time of this element or null if the element is not existing or some error happened.
     */
    Date getLeastRecentUpdateTimeForUuid(String _clientUuid);
}
