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

import org.noorganization.instalistsynch.model.GroupMember;

import java.util.List;

/**
 * Message that is send when the request for Groupmembers to a group was made.
 * Created by tinos_000 on 02.02.2016.
 */
public class GroupMemberListMessageEvent {
    public List<GroupMember> mGroupMembers;
    public int mGroupId;

    public GroupMemberListMessageEvent(List<GroupMember> groupMembers, int deviceId) {
        mGroupMembers = groupMembers;
        mGroupId = deviceId;
    }
}
