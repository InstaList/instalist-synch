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

package org.noorganization.instalistsynch.controller.callback;

/**
 * Callback for
 * Created by tinos_000 on 08.02.2016.
 */
public interface IAuthorizedCallbackCompleted<T> extends ICallbackCompleted<T> {

    /**
     * Called when the user is unauthorized. It fires an {@link org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent} event.
     *
     * @param _groupId the groupId where the accesstoken is not valid.
     */
    void onUnauthorized(int _groupId);
}
