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

/**
 * A event object that holds the authorized group member.
 * Created by Desnoo on 07.02.2016.
 */
public class GroupMemberAuthorizedMessageEvent {

    private GroupMember mGroupMember;
    private boolean mAuthorized;

    public GroupMemberAuthorizedMessageEvent(GroupMember groupMember, boolean authorized) {
        mGroupMember = groupMember;
        mAuthorized = authorized;
    }

    public GroupMember getGroupMember() {
        return mGroupMember;
    }

    public boolean isAuthorized() {
        return mAuthorized;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupMemberAuthorizedMessageEvent that = (GroupMemberAuthorizedMessageEvent) o;

        if (mAuthorized != that.mAuthorized) return false;
        return !(mGroupMember != null ? !mGroupMember.equals(that.mGroupMember) : that.mGroupMember != null);

    }

    @Override
    public int hashCode() {
        int result = mGroupMember != null ? mGroupMember.hashCode() : 0;
        result = 31 * result + (mAuthorized ? 1 : 0);
        return result;
    }
}
