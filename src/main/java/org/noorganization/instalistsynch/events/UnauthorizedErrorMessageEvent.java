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
 * Message is sent when the server responds with a 401-Unauthorized message.
 * Created by Desnoo on 06.02.2016.
 */
public class UnauthorizedErrorMessageEvent {

    /**
     * The id of the group.
     */
    private int mGroupId;

    /**
     * The id of the device.
     */
    private int mDeviceId;


    /**
     * Constructor.
     *
     * @param _groupId  the id of the group.
     * @param _deviceId the id of the device. set to -1 if no device is given.
     */
    public UnauthorizedErrorMessageEvent(int _groupId, int _deviceId) {
        mGroupId = _groupId;
        mDeviceId = _deviceId;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(int deviceId) {
        mDeviceId = deviceId;
    }
}
