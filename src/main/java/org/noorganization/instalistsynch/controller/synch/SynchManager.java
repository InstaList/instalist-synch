package org.noorganization.instalistsynch.controller.synch;

import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.synch.impl.CategorySynch;
import org.noorganization.instalistsynch.controller.synch.impl.ListSynch;
import org.noorganization.instalistsynch.controller.synch.impl.ProductSynch;
import org.noorganization.instalistsynch.controller.synch.impl.TagSynch;
import org.noorganization.instalistsynch.events.CategorySynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.ListSynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.ProductSynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.TagSynchFromNetworkFinished;
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
    private ISynch mListSynch;
    private ISynch mProductSynch;
    private ISynch mTagSynch;

    public SynchManager() {
        EventBus.getDefault().register(this);
        mCategorySynch = new CategorySynch(eModelType.CATEGORY);
        mListSynch = new ListSynch(eModelType.LIST);
        mProductSynch = new ProductSynch(eModelType.PRODUCT);
        mTagSynch = new TagSynch(eModelType.TAG);
    }

    public void init(int _groupId) {

        mCategorySynch.indexLocalEntries(_groupId);
        mListSynch.indexLocalEntries(_groupId);
        mTagSynch.indexLocalEntries(_groupId);
        mProductSynch.indexLocalEntries(_groupId);
    }

    public void synchronize(int _groupId) {
        /*
        IGroupAuthAccessDbController groupAuthAccessDbController =
                LocalSqliteDbControllerFactory.getAuthAccessDbController(
                        GlobalObjects.getInstance()
                                .getApplicationContext());
        GroupAccess groupAccess = groupAuthAccessDbController.getGroupAuthAccess(_groupId);


        ISynch listSynch = new ListSynchObsolete();
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
        GroupAccess groupAccess = groupAuthAccessDbController.getGroupAuthAccess(_groupId);
        Date lastServerUpdate = groupAccess.getLastUpdateFromServer();

        mCategorySynch.indexLocal(_groupId, lastServerUpdate);
        mCategorySynch.synchNetworkToLocal(_groupId, lastServerUpdate);

        // TODO call this at the end !
        GroupAccess groupAuthAccess = groupAuthAccessDbController.getGroupAuthAccess(_groupId);
        groupAuthAccess.setLastUpdateFromServer(new Date());
        groupAuthAccessDbController.update(groupAuthAccess);
    }

    private void synchList(int _groupId, Date _lastServerUpdate) {
        mListSynch.indexLocal(_groupId, _lastServerUpdate);
        mListSynch.synchNetworkToLocal(_groupId, _lastServerUpdate);
    }

    private void synchProduct(int _groupId, Date _lastServerUpdate) {
        mProductSynch.indexLocal(_groupId, _lastServerUpdate);
        mProductSynch.synchNetworkToLocal(_groupId, _lastServerUpdate);
    }

    private void synchTag(int _groupId, Date _lastServerUpdate) {
        mTagSynch.indexLocal(_groupId, _lastServerUpdate);
        mTagSynch.synchNetworkToLocal(_groupId, _lastServerUpdate);
    }

    public void onEvent(CategorySynchFromNetworkFinished _msg) {
        mCategorySynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        synchList(_msg.getGroupId(), _msg.getLastUpdateDate());
    }

    public void onEvent(ListSynchFromNetworkFinished _msg) {
        mListSynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        // todo in here call first unit synch
        synchProduct(_msg.getGroupId(), _msg.getLastUpdateDate());
        synchTag(_msg.getGroupId(), _msg.getLastUpdateDate());
    }

    public void onEvent(ProductSynchFromNetworkFinished _msg) {
        mProductSynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
    }

    public void onEvent(TagSynchFromNetworkFinished _msg) {
        mTagSynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
    }
}
