package org.noorganization.instalistsynch.controller.local.dba;

import android.database.Cursor;

import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.utils.eSORT_MODE;

import java.util.List;

/**
 * The interface to interact with the {@link org.noorganization.instalistsynch.model.GroupAuthAccess} data.
 * Deletion of {@link GroupAuthAccess} is not supported because it cascades on delete with the {@link GroupAuth}.
 * Created by tinos_000 on 30.01.2016.
 */
public interface IGroupAuthAccessDbController {

    /**
     * Return codes of insertion.
     */
    class INSERTION_CODE {
        /**
         * If insertion went good.
         */
        public static final int CORRECT = 0x00000001;
        /**
         * If element already exists.
         */
        public static final int ELEMENT_EXISTS = 0x00000002;
        /**
         * Some general error.
         */
        public static final int ERROR = 0x00000004;
    }

    /**
     * Inserts the given {@link GroupAuthAccess} element.
     *
     * @param _groupAuthAccess the element to insert.
     * @return {@see INSERTION_CODE}
     */
    int insert(GroupAuthAccess _groupAuthAccess);

    /**
     * Get the {@link GroupAuthAccess} object with the given id.
     *
     * @param _groupId the device id.
     * @return the {@link GroupAuthAccess} object associated with the id, or null if there is none with this id.
     */
    GroupAuthAccess getGroupAuthAccess(int _groupId);

    /**
     * Get all {@link GroupAuthAccess} objects.
     *
     * @return all saved access data.
     */
    List<GroupAuthAccess> getGroupAuthAccesses();

    /**
     * Get all {@link GroupAuthAccess} objects.
     *
     * @param _sortMode the mode sort the group items id.
     * @return a cursor to the group authAcess objects.
     */
    Cursor getGroupAuthAccessesCursor(eSORT_MODE _sortMode);

    /**
     * Get all {@link GroupAuthAccess} objects.
     *
     * @param _synchronize indicates if only groups if synchronize enabled be returned. true if only those to synch else those to not synch
     * @return all saved access data.
     */
    List<GroupAuthAccess> getGroupAuthAccesses(boolean _synchronize);

    /**
     * Get {@link GroupAuthAccess} objects since a given time they were updated.
     *
     * @param _sinceTime all objects since this time in ISO8601 format.
     * @return all saved access data.
     */
    List<GroupAuthAccess> getGroupAuthAccesses(String _sinceTime);

    /**
     * Update the entry in the associated database.
     *
     * @param _groupAuthAccess the entry to be updated.
     */
    boolean update(GroupAuthAccess _groupAuthAccess);

    /**
     * Updates the token in the database.
     *
     * @param _groupId  the deviceid to update
     * @param _newToken the value of the new token.
     * @return true if success else false.
     */
    boolean updateToken(int _groupId, String _newToken);

    /**
     * Operation that updates the last access date field to the current date.
     *
     * @param _groupAuthAccess the object to update in db.
     *
    void touch(GroupAuthAccess _groupAuthAccess);
     */

    /**
     * Checks if the given elements device id is already in the database.
     *
     * @param _groupId the object to check for.
     * @return true if it is a new id, false the id already exists.
     */
    boolean hasIdInDatabase(int _groupId);
}