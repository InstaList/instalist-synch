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

package org.noorganization.instalistsynch.controller.synch.comparator;

import org.jetbrains.annotations.NotNull;

/**
 * Interface for the comperators to compare client models with server models.
 * Use this to compare not deleted elements.
 * Created by Desnoo on 16.02.2016.
 */
public interface ISynchComperator<T, S> {
    /**
     * Comparator for two different items.
     *
     * @param _param1 parameter 1 to be compared with param2.
     * @param _param2 parameter 2 to be compared with param1.
     * @return true if equal else if not.
     */
    boolean compare(@NotNull T _param1, @NotNull S _param2);
}
