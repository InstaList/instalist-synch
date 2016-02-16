package org.noorganization.instalistsynch.controller.local.dba;

import android.database.Cursor;

import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.Log;

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
     * Get the last update date of the given uuid in the specified model.
     *
     * @param _uuid      the uuid of the client side model.
     * @param _modelType the type of the model on the client side.
     * @param _type      the type of the model.
     * @param _date      the string of the date.
     * @return a list with matching entries.
     */
    List<Log> getElementByUuid(String _uuid, eActionType _type, eModelType _modelType, String _date);
}
