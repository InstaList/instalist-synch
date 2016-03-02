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

package org.noorganization.instalistsynch.controller.network.impl;

import org.noorganization.instalistsynch.controller.network.IServerAuthenticate;

/**
 * Factory to manage the possible network objects.
 * @deprecated
 * Created by tinos_000 on 27.01.2016.
 */
public class ServerAuthenticationFactory {

    /**
     * Get the default implementation of the authentification with the server.
     * @return the Authenticator instance.
     */
    public static IServerAuthenticate getDefaultServerAuthentication(){
        return ServerAuthtentication.getInstance();
    }
}
