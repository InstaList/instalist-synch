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

package org.noorganization.instalistsynch.controller.local.impl;

import android.util.Log;

import org.noorganization.instalist.comm.message.TokenInfo;
import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.local.IAuthManagerController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.AuthNetworkControllerFactory;
import org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * The DefaultAuthManagerController implementation. It handles the Authentification of a user.
 * Created by tinos_000 on 08.02.2016.
 */
public class DefaultAuthManagerController implements IAuthManagerController {
    private static final String LOG_TAG = DefaultAuthManagerController.class.getSimpleName();

    private static DefaultAuthManagerController sInstance;
    private ISessionController mSessionController;
    private IGroupAuthAccessDbController dbController;

    /**
     * Get the DefaultAuthManagerController.
     *
     * @return the DefaultAuthManagerController instance.
     */
    public static synchronized DefaultAuthManagerController getInstance() {
        if (sInstance == null)
            return new DefaultAuthManagerController();
        return sInstance;
    }

    /**
     * Default private controller constructor.
     */
    private DefaultAuthManagerController() {
        EventBus.getDefault().register(this);
        mSessionController = InMemorySessionController.getInstance();
        dbController = LocalSqliteDbControllerFactory
                .getAuthAccessDbController(GlobalObjects.getInstance()
                        .getApplicationContext());
    }


    @Override
    public void requestToken(int _groupId) {
        Log.i(LOG_TAG, "requestToken: requesting new token for " + _groupId);
        if (_groupId < 0)
            return;

        Boolean inProceeding = GlobalObjects.sCallMapping.get(_groupId);
        if ((inProceeding == null) || !inProceeding) {

            GlobalObjects.sCallMapping.put(_groupId, true);
            GroupAuth groupAuth = LocalSqliteDbControllerFactory
                    .getGroupAuthDbController(GlobalObjects.getInstance()
                            .getApplicationContext()).findById(_groupId);

            if (groupAuth == null)
                return;
            AuthNetworkControllerFactory.getAuthNetworkController().requestAuthToken(new AuthTokenResponse(groupAuth), groupAuth);
        }
    }

    @Override
    public void loadAllSessions() {
        List<GroupAccess> groupAccessList = dbController.getGroupAuthAccesses(true);
        InMemorySessionController.getInstance().loadToken(groupAccessList);
    }

    @Override
    public void invalidateToken(int _groupId) {
        if (_groupId < 0)
            return;

        GlobalObjects.sCallMapping.remove(_groupId);
        mSessionController.removeToken(_groupId);
        dbController.updateToken(_groupId, null);
    }

    public void onEvent(UnauthorizedErrorMessageEvent _msg) {
        if (_msg == null)
            return;

        requestToken(_msg.getGroupId());
    }

    private class AuthTokenResponse implements ICallbackCompleted<TokenInfo> {


        private GroupAuth mGroupAuth;

        /**
         * Constructor of AuthTokenResponse.
         *
         * @param _groupAuth the object that holds this information.
         */
        public AuthTokenResponse(GroupAuth _groupAuth) {
            mGroupAuth = _groupAuth;
        }

        @Override
        public void onCompleted(TokenInfo _next) {
            GlobalObjects.sCallMapping.put(mGroupAuth.getGroupId(), false);
            mSessionController.addOrUpdateToken(mGroupAuth.getGroupId(), _next.getToken());
            dbController.updateToken(mGroupAuth.getGroupId(), _next.getToken());
        }

        @Override
        public void onError(Throwable _e) {
            GlobalObjects.sCallMapping.put(mGroupAuth.getGroupId(), false);
            _e.printStackTrace();
            mSessionController.removeToken(mGroupAuth.getGroupId());
            // dbController.updateToken(mGroupAuth.getGroupId(), null);
        }
    }
}
