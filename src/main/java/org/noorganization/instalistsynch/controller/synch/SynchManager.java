package org.noorganization.instalistsynch.controller.synch;

import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.synch.impl.CategorySynch;
import org.noorganization.instalistsynch.controller.synch.impl.IngredientSynch;
import org.noorganization.instalistsynch.controller.synch.impl.ListEntrySynch;
import org.noorganization.instalistsynch.controller.synch.impl.ListSynch;
import org.noorganization.instalistsynch.controller.synch.impl.ProductSynch;
import org.noorganization.instalistsynch.controller.synch.impl.RecipeSynch;
import org.noorganization.instalistsynch.controller.synch.impl.TagSynch;
import org.noorganization.instalistsynch.controller.synch.impl.UnitSynch;
import org.noorganization.instalistsynch.events.CategorySynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.IngredientSynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.ListEntrySynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.ListSynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.ProductSynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.RecipeSynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.TagSynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.UnitSynchFromNetworkFinished;
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
    //private ISynch mTagSynch;
    private ISynch mIngredientSynch;
    private ISynch mRecipeSynch;
    private ISynch mListEntrySynch;
    private ISynch mUnitSynch;

    private boolean mListSynchDone;
    private boolean mProductSynchDone;
    private boolean mTagSynchDone;
    private boolean mRecipeSynchDone;
    private boolean mUnitSynchDone;
    private boolean mIngredientSynchDone;
    private boolean mListEntrySynchDone;
    private boolean mCategorySynchDone;

    private IGroupAuthAccessDbController mGroupAuthAccessDbController;

    public SynchManager() {
        EventBus.getDefault().register(this);
        mCategorySynch = new CategorySynch(eModelType.CATEGORY);
        mListSynch = new ListSynch(eModelType.LIST);
        mProductSynch = new ProductSynch(eModelType.PRODUCT);
      //  mTagSynch = new TagSynch(eModelType.TAG);
        mIngredientSynch = new IngredientSynch(eModelType.INGREDIENT);
        mRecipeSynch = new RecipeSynch(eModelType.RECIPE);
        mListEntrySynch = new ListEntrySynch(eModelType.LIST_ENTRY);
        mUnitSynch = new UnitSynch();
        mListSynchDone = mProductSynchDone = mTagSynchDone = mRecipeSynchDone = mUnitSynchDone = mIngredientSynchDone = mListEntrySynchDone = mCategorySynchDone = false;
        mGroupAuthAccessDbController =
                LocalSqliteDbControllerFactory.getAuthAccessDbController(GlobalObjects.getInstance().getApplicationContext());
    }

    public void init(int _groupId) {
        mCategorySynch.indexLocalEntries(_groupId);
        mListSynch.indexLocalEntries(_groupId);
        mProductSynch.indexLocalEntries(_groupId);
        //mTagSynch.indexLocalEntries(_groupId);
        mIngredientSynch.indexLocalEntries(_groupId);
        mRecipeSynch.indexLocalEntries(_groupId);
        mListEntrySynch.indexLocalEntries(_groupId);
        mUnitSynch.indexLocalEntries(_groupId);
    }

    public void synchronize(int _groupId) {
        /*if (!isSynchDone()) {
            return;
        }*/

        GroupAccess groupAccess = mGroupAuthAccessDbController.getGroupAuthAccess(_groupId);
        Date lastServerUpdate = groupAccess.getLastUpdateFromServer();

        synchCategory(_groupId, lastServerUpdate);
        synchUnit(_groupId, lastServerUpdate);
        synchTag(_groupId, lastServerUpdate);
        synchRecipe(_groupId, lastServerUpdate);

        groupAccess.setLastUpdateFromServer(new Date());
        mGroupAuthAccessDbController.update(groupAccess);
    }

    public void synchCategory(int _groupId, Date lastServerUpdate) {
        mCategorySynch.indexLocal(_groupId, lastServerUpdate);
        mCategorySynch.synchNetworkToLocal(_groupId, lastServerUpdate);


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
        // mTagSynch.indexLocal(_groupId, _lastServerUpdate);
        // mTagSynch.synchNetworkToLocal(_groupId, _lastServerUpdate);
    }

    private void synchUnit(int _groupId, Date _lastServerUpdate) {
        mUnitSynch.indexLocal(_groupId, _lastServerUpdate);
        mUnitSynch.synchNetworkToLocal(_groupId, _lastServerUpdate);
    }

    private void synchRecipe(int _groupId, Date _lastServerUpdate) {
        mRecipeSynch.indexLocal(_groupId, _lastServerUpdate);
        mRecipeSynch.synchNetworkToLocal(_groupId, _lastServerUpdate);
    }

    private void synchListEntry(int _groupId, Date _lastServerUpdate) {
        mListEntrySynch.indexLocal(_groupId, _lastServerUpdate);
        mListEntrySynch.synchNetworkToLocal(_groupId, _lastServerUpdate);
    }

    private void synchIngredient(int _groupId, Date _lastServerUpdate) {
        mIngredientSynch.indexLocal(_groupId, _lastServerUpdate);
        mIngredientSynch.synchNetworkToLocal(_groupId, _lastServerUpdate);
    }

    public void onEvent(CategorySynchFromNetworkFinished _msg) {
        mCategorySynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        synchList(_msg.getGroupId(), _msg.getLastUpdateDate());
        mCategorySynchDone = true;
        reset(_msg.getGroupId());
    }

    public void onEvent(ListSynchFromNetworkFinished _msg) {
        mListSynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        mListSynchDone = true;
        if (mProductSynchDone) {
            synchListEntry(_msg.getGroupId(), _msg.getLastUpdateDate());
        }
        reset(_msg.getGroupId());
    }

    public void onEvent(ProductSynchFromNetworkFinished _msg) {
        mProductSynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        mProductSynchDone = true;
        if (mListSynchDone) {
            synchListEntry(_msg.getGroupId(), _msg.getLastUpdateDate());
        }
        reset(_msg.getGroupId());
    }

    public void onEvent(TagSynchFromNetworkFinished _msg) {
        //mTagSynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        mTagSynchDone = true;
        reset(_msg.getGroupId());
    }

    public void onEvent(ListEntrySynchFromNetworkFinished _msg) {
        mListEntrySynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        mListEntrySynchDone = true;
        reset(_msg.getGroupId());
    }

    public void onEvent(UnitSynchFromNetworkFinished _msg) {
        mUnitSynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        synchProduct(_msg.getGroupId(), _msg.getLastUpdateDate());
        mUnitSynchDone = true;
        reset(_msg.getGroupId());
    }

    public void onEvent(IngredientSynchFromNetworkFinished _msg) {
        mIngredientSynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        mIngredientSynchDone = true;
        reset(_msg.getGroupId());
    }

    public void onEvent(RecipeSynchFromNetworkFinished _msg) {
        mIngredientSynch.synchLocalToNetwork(_msg.getGroupId(), _msg.getLastUpdateDate());
        mRecipeSynchDone = true;
        if (mProductSynchDone) {
            synchIngredient(_msg.getGroupId(), _msg.getLastUpdateDate());
        }
        reset(_msg.getGroupId());
    }

    private void reset(int _groupId) {
        if (isSynchDone()) {
            mListSynchDone = mProductSynchDone = mTagSynchDone = mRecipeSynchDone = mUnitSynchDone = mIngredientSynchDone = mListEntrySynchDone = mCategorySynchDone = false;
        }
    }

    private boolean isSynchDone() {
        return mListSynchDone
                && mProductSynchDone
                && mTagSynchDone
                && mRecipeSynchDone
                && mUnitSynchDone
                && mIngredientSynchDone
                && mListEntrySynchDone
                && mCategorySynchDone;
    }
}