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

import org.noorganization.instalistsynch.model.TaskErrorLog;

import java.util.List;

/**
 * Management foe TaskErrorlog objects.
 * Created by Desnoo on 15.02.2016.
 */
public interface ITaskErrorLogDbController {

    /**
     * Insert a task error.
     * @param _uuid the server side uuid of the object to be inserted.
     * @param _type the type of the object. ( Shoppinglist, listentry)
     * @param _errorType the type of error {@link org.noorganization.instalistsynch.controller.synch.task.ITask.ReturnCodes}
     * @param _groupId the id of the group.
     * @return true if went good, else false.
     */
    TaskErrorLog insert(String _uuid, int _type, int _errorType, int _groupId);

    /**
     * Get a list of taskerror logs.
     * @param _groupId the id of the group.
     * @param _type the type of the object.
     * @param _errorType the type of error {@link org.noorganization.instalistsynch.controller.synch.task.ITask.ReturnCodes}
     * @return the matching TaskErrorLogs.
     */
    List<TaskErrorLog> get(int _groupId, int _type, int _errorType);

    /**
     * Find the TaskErrorLog by its id.
     * @param _id the id of the error log.
     * @return the TaskErrorLog.
     */
    TaskErrorLog findById(int _id);

    /**
     * Remove entry by its given id.
     * @param _id the id of the element to remove.
     */
    void remove(int _id);
}
