package org.noorganization.instalistsynch.controller.network.model;

/**
 * Interface for each model controller to handle synchronization.
 * Created by tinos_000 on 30.01.2016.
 */
public interface IModelSynchController {

    /**
     * Starts the synchronization of the model.
     * creates an {@link org.noorganization.instalistsynch.events.SynchronizationMessageEvent} and sends it on the eventbus.
     *
     * @param _token   the token to the group.
     * @param _groupId the id of the group to synchronize.
     */
    void startSynchronization(String _token, int _groupId);

    /**
     * Stops the synchronization at the next possible interaction.
     * sends an {@link org.noorganization.instalistsynch.events.SynchronizationMessageEvent} on the eventbus.
     * // TODO this is for the futue ;)
     */
    void stopSynchronization();


}
