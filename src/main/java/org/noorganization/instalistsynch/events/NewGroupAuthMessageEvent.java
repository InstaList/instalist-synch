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

package org.noorganization.instalistsynch.events;

import org.noorganization.instalistsynch.model.GroupAuth;

/**
 * Message when a new group access was established.
 * Created by tinos_000 on 29.01.2016.
 */
public class NewGroupAuthMessageEvent {

    /**
     * The group auth object.
     */
    private GroupAuth mGroupAuth;

    /**
     * Basic Constructor.
     *
     * @param mGroupAuth the group auth object.
     */
    public NewGroupAuthMessageEvent(GroupAuth mGroupAuth) {
        this.mGroupAuth = mGroupAuth;
    }

    /**
     * Get the group auth object.
     *
     * @return the groupauth object.
     */
    public GroupAuth getGroupAuth() {
        return mGroupAuth;
    }

}
