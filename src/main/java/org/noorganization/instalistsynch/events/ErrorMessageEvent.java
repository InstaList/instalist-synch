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
 * Sent when an error occured.
 * Created by tinos_000 on 29.01.2016.
 */
public class ErrorMessageEvent {

    /**
     * The message of the error.
     */
    private String mErrorMessage;

    private int mResourceId;

    /**
     * Default Constructor.
     *
     * @param mErrorMessage the error message to be sent, consider to use a translated string.
     */
    public ErrorMessageEvent(String mErrorMessage) {
        this.mErrorMessage = mErrorMessage;
    }

    /**
     * Create the error with an resource id where the message is.
     *
     * @param _resourceId the id of the resource.
     */
    public ErrorMessageEvent(int _resourceId) {
        mResourceId = _resourceId;
    }


    public int getResourceId() {
        return mResourceId;
    }

    /**
     * Get the error message.
     *
     * @return the error message.
     */
    public String getErrorMessage() {
        return mErrorMessage;
    }
}
