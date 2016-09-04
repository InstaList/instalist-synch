/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.local.dba;

import android.database.Cursor;

import org.noorganization.instalist.types.ActionType;
import org.noorganization.instalist.types.ModelType;
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
    Cursor getLogsSince(String _date, @ModelType.Model int _modelType);

    /**
     * Get the LogInfo entries since a given time for a specified uuid of a element with the type of the specified model.
     *
     * @param _uuid       the uuid of the client side model.
     * @param _modelType  the type of the model on the client side.
     * @param _actionType the type of the action.
     * @param _date       the string of the date.
     * @return a list with matching entries.
     */
    List<LogInfo> getElementByUuid(String _uuid, @ActionType.Action int _actionType, @ModelType.Model int _modelType,
                                   String _date);


    /**
     * Get the least recent update time of the client uuid.
     *
     * @param _clientUuid the uuid of the item to find.
     * @return the last update time of this element or null if the element is not existing or some error happened.
     */
    Date getLeastRecentUpdateTimeForUuid(String _clientUuid);
}
