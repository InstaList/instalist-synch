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

package org.noorganization.instalistsynch.controller.synch;

import java.util.Date;

/**
 * Interface that handles the synchronization of the list on the local side.
 * Created by Desnoo on 14.02.2016.
 */
public interface ISynch {

    /**
     * This method must only be called once before the first sycnhronization with the server.
     * It is a one time action. It indexes all models on client side and assigns them to a specific group
     * or to not a group.
     *
     * @param _groupId could be -1 if the items should be indexed to no group, else the group to which they should be indexed.
     */
    void indexLocalEntries(int _groupId);


    /**
     * Submits the local results to the server.
     *  @param _groupId    the id of the group, to which the results should be synched.
     * @param _lastUpdate the date when the client last sent the local changes.
     */
    void synchLocalToNetwork(int _groupId, Date _lastUpdate);


    /**
     * Indexes each entry since a given time.
     * @param _groupId       the id of the group to index.
     * @param _lastIndexTime the last time and indexing was made.
     */
    void indexLocal(int _groupId, Date _lastIndexTime);

    /**
     * Adds the item to the given group.
     *
     * @param _groupId    the id of the group.
     * @param _clientUuid the uuid of the client.
     */
    void addGroupToMapping(int _groupId, String _clientUuid);

    /**
     * Remove the group form the specified item ( specified by the uuid).
     *
     * @param _groupId    the id of the group.
     * @param _clientUuid the uuid of the client sides items.
     */
    void removeGroupFromMapping(int _groupId, String _clientUuid);

    /**
     * Synchronizes the lists for a given group.
     * It creates, deletes and updates the entries on server side.
     *
     * @param _groupId   the id of the group to synchronize.
     * @param _sinceTime the time when the data was last synched.
     */
    void synchNetworkToLocal(int _groupId, Date _sinceTime);

    /**
     * Resolve the conflict with the given conflict id.
     *
     * @param _conflictId    the id of the conflict.
     * @param _resolveAction the action how the conflict should be resolved.
     */
    void resolveConflict(int _conflictId, int _resolveAction);

}
