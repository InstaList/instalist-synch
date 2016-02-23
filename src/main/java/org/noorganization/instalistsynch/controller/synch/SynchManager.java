package org.noorganization.instalistsynch.controller.synch;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.synch.impl.ListSynch;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.utils.GlobalObjects;

/**
 * Manager that handles all the synchronization work.
 * Created by tinos_000 on 19.02.2016.
 */
public class SynchManager {

    public void index(int _groupId) {
        ISynch listSynch = new ListSynch();
        listSynch.indexLocalEntries(_groupId);
    }

    public void synchronize(int _groupId) {
        IGroupAuthAccessDbController groupAuthAccessDbController =
                LocalSqliteDbControllerFactory.getAuthAccessDbController(
                        GlobalObjects.getInstance()
                                .getApplicationContext());
        GroupAccess groupAccess = groupAuthAccessDbController.getGroupAuthAccess(_groupId);


        ISynch listSynch = new ListSynch();
        listSynch.refreshLocalMapping(_groupId, groupAccess.getLastUpdateFromClient());
        listSynch.synchGroupFromNetwork(_groupId, groupAccess.getLastUpdateFromServer());
        listSynch.synchronizeLocalToNetwork(_groupId,
                ISO8601Utils.format(groupAccess.getLastUpdateFromClient()));
    }
}
