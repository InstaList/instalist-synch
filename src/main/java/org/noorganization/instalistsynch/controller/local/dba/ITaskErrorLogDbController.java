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
     * Remove entry by its given id.
     * @param _id the id of the element to remove.
     */
    void remove(int _id);
}
