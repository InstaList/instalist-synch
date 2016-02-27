package org.noorganization.instalistsynch.controller.synch;

import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.synch.impl.CategorySynch;
import org.noorganization.instalistsynch.events.CategorySynchFromNetworkFinished;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Manager that handles all the synchronization work.
 * Created by tinos_000 on 19.02.2016.
 */
public class SynchManager {
private ISynch mCategorySynch;

    public SynchManager() {
        EventBus.getDefault().register(this);
        mCategorySynch = new CategorySynch(eModelType.CATEGORY);

    }

    public void init(int _groupId) {
        ISynch categorySynch = new CategorySynch(eModelType.CATEGORY);
        categorySynch.indexLocalEntries(_groupId);
    }

    public void synchronize(int _groupId) {
        /*
        IGroupAuthAccessDbController groupAuthAccessDbController =
                LocalSqliteDbControllerFactory.getAuthAccessDbController(
                        GlobalObjects.getInstance()
                                .getApplicationContext());
        GroupAccess groupAccess = groupAuthAccessDbController.getGroupAuthAccess(_groupId);


        ISynch listSynch = new ListSynch();
       // listSynch.refreshLocalMapping(_groupId, groupAccess.getLastUpdateFromClient());
        listSynch.synchNetworkToLocal(_groupId, groupAccess.getLastUpdateFromServer());
        listSynch.synchLocalToNetwork(_groupId,
                ISO8601Utils.format(groupAccess.getLastUpdateFromClient()));*/

        synchCategory(_groupId);
    }


    public void synchCategory(int _groupId) {
        IGroupAuthAccessDbController groupAuthAccessDbController =
                LocalSqliteDbControllerFactory.getAuthAccessDbController(
                        GlobalObjects.getInstance()
                                .getApplicationContext());
        GroupAccess groupAccess      = groupAuthAccessDbController.getGroupAuthAccess(_groupId);
        Date        lastServerUpdate = groupAccess.getLastUpdateFromServer();

        mCategorySynch.indexLocal(_groupId, lastServerUpdate);
        mCategorySynch.synchNetworkToLocal(_groupId, lastServerUpdate);
    }

    public void onEvent(CategorySynchFromNetworkFinished _msg){
        mCategorySynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
    }
}
