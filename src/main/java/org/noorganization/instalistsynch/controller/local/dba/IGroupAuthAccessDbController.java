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

import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.eSortMode;

import java.util.List;

/**
 * The interface to interact with the {@link GroupAccess} data.
 * Deletion of {@link GroupAccess} is not supported because it cascades on delete with the {@link GroupAuth}.
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
     * Inserts the given {@link GroupAccess} element.
     *
     * @param _groupAccess the element to insert.
     * @return {@see INSERTION_CODE}
     */
    int insert(GroupAccess _groupAccess);

    /**
     * Get the {@link GroupAccess} object with the given id.
     *
     * @param _groupId the device id.
     * @return the {@link GroupAccess} object associated with the id, or null if there is none with this id.
     */
    GroupAccess getGroupAuthAccess(int _groupId);

    /**
     * Get all {@link GroupAccess} objects.
     *
     * @return all saved access data.
     */
    List<GroupAccess> getGroupAuthAccesses();

    /**
     * Get all {@link GroupAccess} objects.
     *
     * @param _sortMode the mode sort the group items id.
     * @return a cursor to the group authAcess objects.
     */
    Cursor getGroupAuthAccessesCursor(eSortMode _sortMode);

    /**
     * Get all {@link GroupAccess} objects.
     *
     * @param _synchronize indicates if only groups if synchronize enabled be returned. true if only those to synch else those to not synch
     * @return all saved access data.
     */
    List<GroupAccess> getGroupAuthAccesses(boolean _synchronize);

    /**
     * Get {@link GroupAccess} objects since a given time they were updated.
     *
     * @param _sinceTime all objects since this time in ISO8601 format.
     * @return all saved access data.
     */
    List<GroupAccess> getGroupAuthAccesses(String _sinceTime);

    /**
     * Update the entry in the associated database.
     *
     * @param _groupAccess the entry to be updated.
     */
    boolean update(GroupAccess _groupAccess);

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
    void touch(GroupAccess _groupAuthAccess);
     */

    /**
     * Checks if the given elements device id is already in the database.
     *
     * @param _groupId the object to check for.
     * @return true if it is a new id, false the id already exists.
     */
    boolean hasIdInDatabase(int _groupId);
}
