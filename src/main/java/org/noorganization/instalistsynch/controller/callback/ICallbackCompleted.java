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
 * Callback when a network request was finally parsed. Use it for a callback of unauthorized callbacks.
 * Created by tinos_000 on 08.02.2016.
 */
public interface ICallbackCompleted<T> {

    /**
     * Called when the request was finally done.
     *
     * @param _next the object that was parsed.
     */
    void onCompleted(T _next);

    /**
     * The error when an error happened.
     *
     * @param _e the error.
     */
    void onError(Throwable _e);
}
