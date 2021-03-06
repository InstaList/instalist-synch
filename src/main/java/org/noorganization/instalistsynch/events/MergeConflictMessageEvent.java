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

/**
 * Sent when there was a merge conflict that the user should solve.
 * Created by Desnoo on 15.02.2016.
 */
public class MergeConflictMessageEvent {

    public int mConflictId;
    public String mUUID;

    public MergeConflictMessageEvent(int _conflictId, String _uuid) {
        mConflictId = _conflictId;
        mUUID = _uuid;
    }
}
