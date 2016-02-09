package org.noorganization.instalistsynch.controller.network.impl;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.network.ISynchController;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
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
        List<GroupAuthAccess> groupAuthAccessList = mGroupAuthAccessDbController
                .getGroupAuthAccesses(true);

        if (groupAuthAccessList.size() == 0) {
            Log.e(LOG_TAG, "synchronizeAllGroups: no group auths to synch");
            return;
        }

        for (GroupAuthAccess groupAuthAccessElem : groupAuthAccessList) {
            synchronizeGroup(groupAuthAccessElem);
        }
    }

    @Override
    public void synchronizeGroup(@NonNull GroupAuth _groupAuth) {
        GroupAuthAccess groupAuthAccess = mGroupAuthAccessDbController.getGroupAuthAccess(_groupAuth.getDeviceId());
        if(groupAuthAccess == null){
            //TODO show some error
            Log.e(LOG_TAG, "synchronizeAllGroups: no groupAuthAccess for given groupAuth " + _groupAuth.toString());
            return;
        }

        synchronizeGroup(groupAuthAccess);
    }

    /**
     * Does the whole synchronization process.
     * @param _groupAuthAccess the group object to be synchronized.
     * @return true if synch runs through correct, else false.
     */
    private boolean synchronizeGroup(@NonNull GroupAuthAccess _groupAuthAccess){

        return false;
    }
}
