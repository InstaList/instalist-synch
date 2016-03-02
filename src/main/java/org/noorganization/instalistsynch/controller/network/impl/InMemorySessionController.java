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

import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.model.GroupAccess;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

/**
 * The InMemorySessionManager that holds the session info inside the cache.
 * Sessions are updated from the {@link org.noorganization.instalistsynch.controller.local.IAuthManagerController}.
 * Created by tinos_000 on 08.02.2016.
 */
public class InMemorySessionController implements ISessionController {

    /**
     * All cached sessions.
     */
    private ConcurrentHashMap<Integer, String> mSessions;

    /**
     * Instance member.
     */
    private static InMemorySessionController sInstance;

    /**
     * Get the instance of this DefaultSessionManager.
     *
     * @return the single instance of this class.
     */
    public static InMemorySessionController getInstance() {
        if (sInstance == null) {
            sInstance = new InMemorySessionController();
        }
        return sInstance;
    }

    /**
     * Default private constructor.
     */
    private InMemorySessionController() {
        mSessions = new ConcurrentHashMap<>();
    }

    @Override
    public String getToken(int _groupId) {
        return mSessions.get(_groupId);
    }

    @Override
    public void addOrUpdateToken(int _groupId, String _token) {
        mSessions.put(_groupId, _token);
        EventBus.getDefault().post(new TokenMessageEvent(_token, _groupId));
    }

    @Override
    public void removeToken(int _groupId) {
        mSessions.remove(_groupId);
    }

    @Override
    public void loadToken(List<GroupAccess> _accessTokenPairs) {
        for (GroupAccess accessTokenPair : _accessTokenPairs) {
            mSessions.put(accessTokenPair.getGroupId(), accessTokenPair.getToken());
        }
    }
}
