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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.network.ISynchController;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.util.List;

/**
 * The default sychronization controller.
 * Created by tinos_000 on 30.01.2016.
 */
public class DefaultSynchController implements ISynchController {
    private static final String LOG_TAG = DefaultSynchController.class.getSimpleName();
    private static DefaultSynchController sInstance;

    private Context mContext;
    private IGroupAuthAccessDbController mGroupAuthAccessDbController;

    /**
     * Get an instance of this object.
     *
     * @return the instance of this class.
     */
    public static DefaultSynchController getInstance() {
        if (sInstance == null) {
            sInstance = new DefaultSynchController();
        }
        return sInstance;
    }

    private DefaultSynchController() {
        mContext = GlobalObjects.getInstance().getApplicationContext();
        mGroupAuthAccessDbController = LocalSqliteDbControllerFactory
                .getAuthAccessDbController(mContext);
    }

    @Override
    public void synchronizeAllGroups() {
        List<GroupAccess> groupAccessList = mGroupAuthAccessDbController
                .getGroupAuthAccesses(true);

        if (groupAccessList.size() == 0) {
            Log.e(LOG_TAG, "synchronizeAllGroups: no group auths to synch");
            return;
        }

        for (GroupAccess groupAccessElem : groupAccessList) {
            synchronizeGroup(groupAccessElem);
        }
    }

    @Override
    public void synchronizeGroup(@NonNull GroupAuth _groupAuth) {
        GroupAccess groupAccess = mGroupAuthAccessDbController.getGroupAuthAccess(_groupAuth.getDeviceId());
        if(groupAccess == null){
            //TODO show some error
            Log.e(LOG_TAG, "synchronizeAllGroups: no groupAccess for given groupAuth " + _groupAuth.toString());
            return;
        }

        synchronizeGroup(groupAccess);
    }

    /**
     * Does the whole synchronization process.
     * @param _groupAccess the group object to be synchronized.
     * @return true if synch runs through correct, else false.
     */
    private boolean synchronizeGroup(@NonNull GroupAccess _groupAccess){

        return false;
    }
}
