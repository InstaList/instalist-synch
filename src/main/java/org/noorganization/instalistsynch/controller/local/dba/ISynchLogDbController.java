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

import java.util.Date;

/**
 * Used to log the synchronisation process to a database.
 * Created by tinos_000 on 10.02.2016.
 */
public interface ISynchLogDbController {

    /**
     * Insert into the log.
     *
     * @param _groupId    the id of the group.
     * @param _modelType  the type of the model.
     * @param _actionType the type of action.
     * @param _uuidLocal  the local uuid.
     * @param _uuidRemote the remote uuid.
     * @return true if insertion went good false if not.
     */
    boolean insertAction(int _groupId, @ModelType.Model int _modelType, @ActionType.Action int _actionType, String _uuidLocal, String _uuidRemote);

    /**
     * Clear the log before the specified time.
     *
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
    Cursor getLog(int _groupId, @ModelType.Model int _modelType, @ActionType.Action int _actionType);

}
