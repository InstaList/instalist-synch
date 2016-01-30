package org.noorganization.instalistsynch.controller.network.model;

import org.noorganization.instalistsynch.model.GroupAuthAccess;

/**
 * Interface for each model controller to handle synchronization.
 * Created by tinos_000 on 30.01.2016.
 */
public interface IModelSynchController {

    /**
     * Starts the synchronization of the model.
     * creates an {@link org.noorganization.instalistsynch.events.SynchronizationMessageEvent} and sends it on the eventbus.
     * @param _groGroupAuthAccess the access object for the group to synchronize.
     */
    void startSynchronization(GroupAuthAccess _groGroupAuthAccess);

    /**
     * Stops the synchronization at the next possible interaction.
     * sends an {@link org.noorganization.instalistsynch.events.SynchronizationMessageEvent} on the eventbus.
     * // TODO this is for the futue ;)
     */
    void stopSynchronization();


}
