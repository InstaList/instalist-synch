package org.noorganization.instalistsynch.controller.synch;

import java.util.Date;

/**
 * Interface that handles the synchronization of the list on the local side.
 * Created by Desnoo on 14.02.2016.
 */
public interface ILocalListSynch {

    /**
     * This is used to refreh the local mapping before the synch process.
     * It updates all entries in the mapping table.
     * @param _sinceTime
     */
    void refreshLocalMapping(Date _sinceTime);

    /**
     * Synchronizes the lists for a given group.
     *
     * @param _groupId the id of the group to synchronize.
     */
    void synchGroupFromNetwork(int _groupId);

    /**
     * Resolve the conflict with the given conflict id.
     *
     * @param _conflictId    the id of the conflict.
     * @param _resolveAction the action how the conflict should be resolved.
     */
    void resolveConflict(int _conflictId, int _resolveAction);

}
