package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalistsynch.model.GroupAuth;

/**
 * Interface for Synchronization interaction. To trigger Synching.
 * Created by tinos_000 on 30.01.2016.
 */
public interface ISynchController {
    /**
     * Trigger Synchronization of all groups.
     */
    void synchronizeAllGroups();

    /**
     * Trigger synchronization of a specific group.
     * @param _groupAuth the object for group access.
     */
    void synchronizeGroup(GroupAuth _groupAuth);

    /**
     * Stop Synchronization process.
     *
    void stopSynchronizationProcess();

    TODO maybe later
    boolean synchronizationWasInterrupted();
    void snychronizeInterrupted();
    */
}
