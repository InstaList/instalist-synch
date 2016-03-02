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

package org.noorganization.instalistsynch.controller.network;

import org.noorganization.instalist.comm.message.TokenInfo;
import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.model.GroupAuth;

/**
 * Manager that handles network interaction for authentification and authorization.
 * Created by Desnoo on 06.02.2016.
 */
public interface IAuthNetworkController {

    /**
     * Request an auth token.
     *
     * @param _groupAuth the object that holds the whole auth information.
     */
    void requestAuthToken(ICallbackCompleted<TokenInfo> _callback, GroupAuth _groupAuth);

}
