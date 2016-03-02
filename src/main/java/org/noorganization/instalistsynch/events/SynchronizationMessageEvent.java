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
 * The synchronizationMessageEvent is sent when the synchronization was successfull or interrupted.
 * Created by tinos_000 on 30.01.2016.
 */
public class SynchronizationMessageEvent {

    /**
     * The affected model class
     */
    public Class mClass;

    /**
     * Indicates if synch was successful or not.
     */
    public boolean mSuccessful;

    /**
     * Message to be displayed by on error.
     */
    public int mStringResourceId;

    /**
     * Constructor.
     * @param _Class the class which was synched.
     * @param _successful true if successful else false.
     */
    public SynchronizationMessageEvent(Class _Class, boolean _successful, int _stringResourceId) {
        mClass = _Class;
        mSuccessful = _successful;
        mStringResourceId = _stringResourceId;
    }
}
