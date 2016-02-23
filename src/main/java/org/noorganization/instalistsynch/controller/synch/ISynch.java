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
     *
     * @param _groupId    the id of the group, to which the results should be synched.
     * @param _lastUpdate the date when the client last sent the local changes.
     */
    void synchronizeLocalToNetwork(int _groupId, String _lastUpdate);

    /**
     * This is used to refresh the local mapping before each synch process.
     * It updates all entries in the mapping table.
     * !Important! it refreshes only the local entries, the server synch process is managed by another function!
     *
     * @param _groupId
     * @param _sinceTime
     */
    void refreshLocalMapping(int _groupId, Date _sinceTime);

    /**
     * Synchronizes the lists for a given group.
     * It creates, deletes and updates the entries on server side.
     *
     * @param _groupId   the id of the group to synchronize.
     * @param _sinceTime the time when the data was last synched.
     */
    void synchGroupFromNetwork(int _groupId, Date _sinceTime);

    /**
     * Resolve the conflict with the given conflict id.
     *
     * @param _conflictId    the id of the conflict.
     * @param _resolveAction the action how the conflict should be resolved.
     */
    void resolveConflict(int _conflictId, int _resolveAction);

}
