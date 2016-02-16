package org.noorganization.instalistsynch.controller.synch;

/**
 * Interface that handles the synchronization of the list on the local side.
 * Created by Desnoo on 14.02.2016.
 */
public interface ILocalListSynch {

    /**
     * This is used to initialize the local synch process.
     * This should only be started at the begin, when the client is up to create the first group.
     * It creates all neccessary entries in the database.
     */
    void initSynch();

    /**
     * Synchronizes the lists for a given group.
     *
     * @param _groupId the id of the group to synchronize.
     */
    void synchGroupFromNetwork(int _groupId);

    /**
     * Resolve the conflict with the given conflict id.
     * @param _conflictId the id of the conflict.
     * @param _resolveAction the action how the conflict should be resolved.
     */
    void resolveConflict(int _conflictId, int _resolveAction);

}
