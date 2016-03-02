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
