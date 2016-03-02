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
 * Sent when the creation of the group failed, caused by a network error.
 * Created by tinos_000 on 08.02.2016.
 */
public class CreateGroupNetworkExceptionMessageEvent {
    public String mDeviceName;
    public int mAttempt;

    public CreateGroupNetworkExceptionMessageEvent(String _deviceName, int _attempt) {
        mDeviceName = _deviceName;
        mAttempt = _attempt;
    }
}
