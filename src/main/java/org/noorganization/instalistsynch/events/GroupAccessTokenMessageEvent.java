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
 * This event is send when a temporary GroupAccessToken was requested.
 * Created by tinos_000 on 02.02.2016.
 */
public class GroupAccessTokenMessageEvent {

    private int mGroupId;
    private String mGroupAccessToken;

    public GroupAccessTokenMessageEvent(int _groupId, String _groupAccessToken) {
        mGroupAccessToken = _groupAccessToken;
        mGroupId = _groupId;
    }

    public String getGroupAccessToken() {
        return mGroupAccessToken;
    }

    public void setGroupAccessToken(String groupAccessToken) {
        mGroupAccessToken = groupAccessToken;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupAccessTokenMessageEvent)) return false;

        GroupAccessTokenMessageEvent that = (GroupAccessTokenMessageEvent) o;

        if (getGroupId() != that.getGroupId()) return false;
        return !(getGroupAccessToken() != null ? !getGroupAccessToken().equals(that.getGroupAccessToken()) : that.getGroupAccessToken() != null);

    }

    @Override
    public int hashCode() {
        int result = getGroupId();
        result = 31 * result + (getGroupAccessToken() != null ? getGroupAccessToken().hashCode() : 0);
        return result;
    }
}
