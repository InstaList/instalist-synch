package org.noorganization.instalistsynch.controller.synch.impl;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.EntryInfo;
import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.ListEntry;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.IProductController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;
import org.noorganization.instalistsynch.controller.local.dba.IClientLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.ITaskErrorLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.TaskErrorLogDbController;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.model.INetworkController;
import org.noorganization.instalistsynch.controller.network.model.RemoteModelAccessControllerFactory;
import org.noorganization.instalistsynch.controller.synch.ISynch;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.events.ListEntrySynchFromNetworkFinished;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.model.TaskErrorLog;
import org.noorganization.instalistsynch.utils.Constants;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;

/**
 * Synchronization of all lists.
 * Created by Desnoo on 27.02.2016.
 */
public class ListEntrySynch implements ISynch {
    private static final String TAG = "IngredientSynch";

    private ISessionController mSessionController;
    private IListController mListController;
    private IProductController mProductController;

    private IModelMappingDbController mListEntryMapping;
    private IModelMappingDbController mListModelMapping;
    private IModelMappingDbController mProductModelMapping;

    private IClientLogDbController mClientLogDbController;
    private IGroupAuthDbController mGroupAuthDbController;
    private INetworkController<EntryInfo> mListEntryInfoNetworkController;
    private ITaskErrorLogDbController mTaskErrorLogDbController;

    private eModelType mModelType;
    private EventBus mEventBus;

    public ListEntrySynch(eModelType _type) {
        mModelType = _type;

        Context context = GlobalObjects.getInstance().getApplicationContext();
        mSessionController = InMemorySessionController.getInstance();
        mListController = ControllerFactory.getListController(context);
        mProductController = ControllerFactory.getProductController(context);

        mListEntryMapping = ModelMappingDbFactory.getInstance().getSqliteListEntryMappingController();
        mListModelMapping = ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController();
        mProductModelMapping = ModelMappingDbFactory.getInstance().getSqliteProductMappingController();

        mClientLogDbController = LocalSqliteDbControllerFactory.getClientLogController(context);
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(context);
        mListEntryInfoNetworkController = RemoteModelAccessControllerFactory.getInstance().getListEntryNetworkController();
        mTaskErrorLogDbController = TaskErrorLogDbController.getInstance(context);
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void indexLocalEntries(int _groupId) {
        List<ModelMapping> modelMappings = mListEntryMapping.get(null, null);
        if (modelMappings.size() > 0) {
            return;
        }

        List<ShoppingList> shoppingListList = mListController.getAllLists();

        ModelMapping modelMapping;
        for (ShoppingList list : shoppingListList) {
            List<ListEntry> listEntries = mListController.listAllListEntries(list.mUUID, list.mCategory == null ? null : list.mCategory.mUUID);
            for (ListEntry listEntry : listEntries) {
                modelMapping =
                        new ModelMapping(null, _groupId, null, listEntry.mUUID, new Date(Constants.INITIAL_DATE), new Date(), false);
                mListEntryMapping.insert(modelMapping);
            }
        }

    }

    @Override
    public void indexLocal(int _groupId, Date _lastIndexTime) {
        String lastIndexTime = ISO8601Utils.format(_lastIndexTime, false, TimeZone.getTimeZone("GMT+0000"));//.concat("+0000");
        boolean isLocal = false;
        GroupAuth groupAuth = mGroupAuthDbController.getLocalGroup();
        if (groupAuth != null) {
            isLocal = groupAuth.getGroupId() == _groupId;
        }
        Cursor logCursor =
                mClientLogDbController.getLogsSince(lastIndexTime, mModelType);
        if (logCursor.getCount() == 0) {
            logCursor.close();
            return;
        }

        try {
            while (logCursor.moveToNext()) {
                // fetch the action type
                int actionId = logCursor.getInt(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION));
                eActionType actionType = eActionType.getTypeById(actionId);

                List<ModelMapping> modelMappingList = mListEntryMapping.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND " +
                                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?", new String[]{
                                String.valueOf(_groupId),
                                logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID))});
                ModelMapping modelMapping =
                        modelMappingList.size() == 0 ? null : modelMappingList.get(0);

                switch (actionType) {
                    case INSERT:
                        // skip insertion because this should be decided by the user if the non local groups should have access to the category
                        // and also skip if a mapping for this case already exists!
                        if (!isLocal || modelMapping != null) {
                            continue;
                        }

                        String clientUuid = logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID));
                        Date clientDate = ISO8601Utils.parse(logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE)), new ParsePosition(0));
                        modelMapping = new ModelMapping(null, groupAuth.getGroupId(), null, clientUuid, new Date(Constants.INITIAL_DATE), clientDate, false);
                        mListEntryMapping.insert(modelMapping);
                        break;
                    case UPDATE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        String timeString = logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mListEntryMapping.update(modelMapping);
                        break;
                    case DELETE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        modelMapping.setDeleted(true);
                        timeString = logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mListEntryMapping.update(modelMapping);
                        break;
                    default:
                }

            }
        } catch (Exception e) {
            logCursor.close();
        }
    }

    @Override
    public void addGroupToMapping(int _groupId, String _clientUuid) {
        Date lastUpdate = mClientLogDbController.getLeastRecentUpdateTimeForUuid(_clientUuid);
        if (lastUpdate == null) {
            return;
        }
        ModelMapping modelMapping = new ModelMapping(null, _groupId, null, _clientUuid, new Date(Constants.INITIAL_DATE), lastUpdate, false);
        mListEntryMapping.insert(modelMapping);
    }

    @Override
    public void removeGroupFromMapping(int _groupId, String _clientUuid) {
        List<ModelMapping> modelMappingList = mListEntryMapping.get(
                ModelMapping.COLUMN.GROUP_ID
                        + " = ? AND " + ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?",
                new String[]{String.valueOf(_groupId), _clientUuid});
        if (modelMappingList.size() == 0) {
            return;
        }
        mListEntryMapping.delete(modelMappingList.get(0));
    }

    @Override
    public void synchLocalToNetwork(int _groupId, Date _lastUpdate) {
        String lastUpdateString = ISO8601Utils.format(_lastUpdate, false, TimeZone.getTimeZone("GMT+0000"));
        String authToken = mSessionController.getToken(_groupId);

        if (authToken == null) {
            // todo do some caching of this action
            return;
        }

        List<ModelMapping> modelMappingList = mListEntryMapping.get(
                ModelMapping.COLUMN.LAST_CLIENT_CHANGE + " >= ? ", new String[]{lastUpdateString});
        for (ModelMapping modelMapping : modelMappingList) {
            if (modelMapping.isDeleted()) {
                // delete the item
                mListEntryInfoNetworkController.deleteItem(new DeleteResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, modelMapping.getServerSideUUID(), authToken);
            } else if (modelMapping.getServerSideUUID() == null) {
                // insert new
                ListEntry listEntry = mListController.getEntryById(modelMapping.getClientSideUUID());
                if (listEntry == null) {
                    continue;
                }

                EntryInfo listEntryInfo = getListEntryInfo(listEntry, _groupId, modelMapping);
                if (listEntryInfo == null)
                    continue;

                mListEntryInfoNetworkController.createItem(new InsertResponse(modelMapping, listEntryInfo.getUUID()), _groupId, listEntryInfo, authToken);
            } else {
                // update existing
                ListEntry listEntry = mListController.getEntryById(modelMapping.getClientSideUUID());
                if (listEntry == null) {
                    // probably the item was deleted
                    mListEntryInfoNetworkController.deleteItem(new DeleteResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, modelMapping.getServerSideUUID(), authToken);
                    continue;
                }
                EntryInfo entryInfo = getListEntryInfo(listEntry, _groupId, modelMapping);
                if (entryInfo == null)
                    continue;

                mListEntryInfoNetworkController.updateItem(new UpdateResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, entryInfo.getUUID(), entryInfo, authToken);
            }
        }
    }


    /**
     * Get the ingredient info from the ingredient model.
     *
     * @param _listEntry    the listentry that infos are extracted.
     * @param _groupId      the id of the group.
     * @param _modelMapping the ingredient model mapping.
     * @return the related IngredientInfo object or null if something failed.
     */
    private EntryInfo getListEntryInfo(ListEntry _listEntry, int _groupId, ModelMapping _modelMapping) {
        EntryInfo entryInfo = new EntryInfo();
        String uuid = mListEntryMapping.generateUuid();

        entryInfo.setUUID(uuid);
        entryInfo.setAmount(_listEntry.mAmount);
        entryInfo.setStruck(_listEntry.mStruck);
        entryInfo.setPriority(_listEntry.mPriority);

        String productUuid = _listEntry.mProduct.mUUID;
        String listUuid = _listEntry.mList.mUUID;

        // fetch the mapping of product and recipe to know the id on the server side.
        List<ModelMapping> productMappingList = mProductModelMapping.get(ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ? ",
                new String[]{productUuid, String.valueOf(_groupId)});
        List<ModelMapping> listMappingList = mListModelMapping.get(ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ? ",
                new String[]{listUuid, String.valueOf(_groupId)});

        if (productMappingList.size() == 0 || listMappingList.size() == 0) {
            // todo indicate that there is a resource missing
            return null;
        }

        ModelMapping productMapping = productMappingList.get(0);
        ModelMapping listMapping = listMappingList.get(0);

        if (productMapping.getServerSideUUID() == null || listMapping.getServerSideUUID() == null) {
            return null;
        }
        entryInfo.setProductUUID(productMapping.getServerSideUUID());
        entryInfo.setListUUID(listMapping.getServerSideUUID());

        Date lastChanged = new Date(_modelMapping.getLastClientChange().getTime()-Constants.NETWORK_OFFSET);
        entryInfo.setLastChanged(lastChanged);
        entryInfo.setDeleted(false);
        return entryInfo;
    }

    /**
     * Get the requested model mapping.
     *
     * @param _columnName          the affected column for the where clause.
     * @param _firstWhereParameter the parameter for this where clause
     * @param _groupId             the id of the group.
     * @return the model mapping or null if there exists none.
     */
    private ModelMapping getModelMapping(IModelMappingDbController modelMappingDbController, String _columnName, String _firstWhereParameter, int _groupId) {
        List<ModelMapping> mappingList = modelMappingDbController.get(_columnName + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ? ",
                new String[]{_firstWhereParameter, String.valueOf(_groupId)});
        if (mappingList.size() == 0) {
            // todo indicate that there is a resource missing
            return null;
        }

        return mappingList.get(0);
    }

    @Override
    public void synchNetworkToLocal(int _groupId, Date _sinceTime) {
        String authToken = mSessionController.getToken(_groupId);
        if (authToken == null) {
            return;
        }
        mListEntryInfoNetworkController.getList(new GetListResponse(_groupId, _sinceTime), _groupId, ISO8601Utils.format(_sinceTime, false, TimeZone.getTimeZone("GMT+0000")).concat("+0000"), authToken);
    }

    @Override
    public void resolveConflict(int _conflictId, int _resolveAction) {
        TaskErrorLog log = mTaskErrorLogDbController.findById(_conflictId);
        if (log == null) {
            return;
        }
        String authToken = mSessionController.getToken(log.getGroupId());
        if (authToken == null) {
            return;
        }

        mListEntryInfoNetworkController.getItem(new GetItemConflictResolveResponse(_resolveAction, _conflictId, log.getGroupId()), log.getGroupId(), log.getUUID(), authToken);
    }

    private class DeleteResponse implements IAuthorizedCallbackCompleted<Void> {

        private String mServerSideUuid;
        private ModelMapping mModelMapping;

        public DeleteResponse(ModelMapping _modelMapping, String _serverSideUuid) {
            mModelMapping = _modelMapping;
            mServerSideUuid = _serverSideUuid;
        }

        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(Void _next) {
            mListEntryMapping.delete(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class InsertResponse implements IAuthorizedInsertCallbackCompleted<Void> {

        private String mServerSideUuid;
        private ModelMapping mModelMapping;

        public InsertResponse(ModelMapping _modelMapping, String _serverSideUuid) {
            mModelMapping = _modelMapping;
            mServerSideUuid = _serverSideUuid;
        }

        @Override
        public void onConflict() {
            // todo
        }


        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(Void _next) {
            mModelMapping.setLastServerChanged(new Date());
            mModelMapping.setServerSideUUID(mServerSideUuid);
            mListEntryMapping.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class UpdateResponse implements IAuthorizedCallbackCompleted<Void> {

        private String mServerSideUuid;
        private ModelMapping mModelMapping;

        public UpdateResponse(ModelMapping _modelMapping, String _serverSideUuid) {
            mModelMapping = _modelMapping;
            mServerSideUuid = _serverSideUuid;
        }


        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(Void _next) {
            mListEntryMapping.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class GetListResponse implements IAuthorizedCallbackCompleted<List<EntryInfo>> {

        private int mGroupId;
        private Date mLastUpdateDate;

        public GetListResponse(int _groupId, Date _lastUpdateDate) {
            mGroupId = _groupId;
            mLastUpdateDate = _lastUpdateDate;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new ListEntrySynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onCompleted(List<EntryInfo> _next) {
            for (EntryInfo listEntryInfo : _next) {
                List<ModelMapping> modelMappingList = mListEntryMapping.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), listEntryInfo.getUUID()});

                if (modelMappingList.size() == 0) {
                    ModelMapping listMapping = getModelMapping(mListModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, listEntryInfo.getListUUID(), mGroupId);
                    ModelMapping productMapping = getModelMapping(mProductModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, listEntryInfo.getProductUUID(), mGroupId);

                    if (listMapping == null || productMapping == null) {
                        // todo not inserted info maybe requery this stuff
                        continue;
                    }

                    ShoppingList list = mListController.getListById(listMapping.getClientSideUUID());
                    Product product = mProductController.findById(productMapping.getClientSideUUID());
                    // new entry
                    ListEntry listEntry = mListController.addOrChangeItem(list, product, listEntryInfo.getAmount(), listEntryInfo.getPriority(), false);
                    if (listEntry == null) {
                        // TODO some error happened
                        continue;
                    }
                    ModelMapping modelMapping = new ModelMapping(null, mGroupId, listEntryInfo.getUUID(),
                            listEntry.mUUID, listEntryInfo.getLastChanged(), listEntryInfo.getLastChanged(), false);
                    mListEntryMapping.insert(modelMapping);
                } else {
                    ModelMapping listMapping = getModelMapping(mListModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, listEntryInfo.getListUUID(), mGroupId);
                    ModelMapping productMapping = getModelMapping(mProductModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, listEntryInfo.getProductUUID(), mGroupId);

                    if (listMapping == null || productMapping == null) {
                        // todo not inserted info maybe requery this stuff
                        continue;
                    }

                    ShoppingList list = mListController.getListById(listMapping.getClientSideUUID());
                    Product product = mProductController.findById(productMapping.getClientSideUUID());
                    // new entry
                    ListEntry listEntry = mListController.addOrChangeItem(list, product, listEntryInfo.getAmount(), listEntryInfo.getPriority(), false);
                    if (listEntry == null) {
                        // TODO some error happened
                        continue;
                    }

                    // entry exists local
                    ModelMapping modelMapping = modelMappingList.get(0);
                    ListEntry listEntry2 = mListController.getEntryById(modelMapping.getClientSideUUID());

                    if (listEntryInfo.getDeleted()) {
                        // was deleted on server side
                        mListController.removeItem(listEntry2);
                        mListEntryMapping.delete(modelMapping);
                        continue;
                    }

                    // else there was an update!
                    if (modelMapping.getLastClientChange().after(listEntryInfo.getLastChanged())) {
                        // use server side or client side, let the user decide
                        mTaskErrorLogDbController.insert(listEntryInfo.getUUID(), mModelType.ordinal(), ITask.ReturnCodes.MERGE_CONFLICT, mGroupId);
                        continue;
                    }

                    ListEntry listEntry1 = mListController.addOrChangeItem(list, product, listEntryInfo.getAmount(), listEntryInfo.getPriority(), false);
                    if (listEntry1 == null) {
                        Log.e(TAG, "onCompleted: update of ingredient from server went wrong.");
                        continue;
                    }
                    if (listEntryInfo.getStruck()) {
                        mListController.strikeItem(listEntry1);
                    } else {
                        mListController.unstrikeItem(listEntry1);
                    }
                    Date lastChanged = new Date(listEntryInfo.getLastChanged().getTime()-Constants.NETWORK_OFFSET);
                    modelMapping.setLastServerChanged(lastChanged);

                    mListEntryMapping.update(modelMapping);
                }
            }

            EventBus.getDefault().post(new ListEntrySynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new ListEntrySynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }
    }

    private class GetItemConflictResolveResponse implements IAuthorizedCallbackCompleted<EntryInfo> {
        private int mResolveAction;
        private int mCaseId;
        private int mGroupId;

        public GetItemConflictResolveResponse(int _resolveAction, int _caseId, int _groupId) {
            mResolveAction = _resolveAction;
            mCaseId = _caseId;
            mGroupId = _groupId;
        }

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(EntryInfo _next) {
            if (mResolveAction == ITask.ResolveCodes.RESOLVE_USE_CLIENT_SIDE) {
                // use client side
                // no further action needed?
            } else {
                // use server side
                List<ModelMapping> modelMappingList = mListEntryMapping.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), _next.getUUID()});
                if (modelMappingList.size() == 0) {
                    return;
                }

                ModelMapping modelMapping = modelMappingList.get(0);

                ModelMapping listMapping = getModelMapping(mListModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, _next.getListUUID(), mGroupId);
                ModelMapping productMapping = getModelMapping(mProductModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, _next.getProductUUID(), mGroupId);

                if (listMapping == null || productMapping == null) {
                    // todo not inserted info maybe requery this stuff
                    return;
                }

                ShoppingList list = mListController.getListById(listMapping.getClientSideUUID());
                Product product = mProductController.findById(productMapping.getClientSideUUID());

                // entry exists local
                ListEntry ingredient = mListController.getEntryById(modelMapping.getClientSideUUID());

                ListEntry listEntry = mListController.addOrChangeItem(list, product, _next.getAmount(), _next.getPriority(), false);
                if (_next.getStruck()) {
                    mListController.strikeItem(listEntry);
                } else {
                    mListController.unstrikeItem(listEntry);
                }
                if (listEntry == null) {
                    Log.e(TAG, "onCompleted: update of ingredient from server went wrong.");
                }
                modelMapping.setLastServerChanged(_next.getLastChanged());

                mListEntryMapping.update(modelMapping);

            }
            mTaskErrorLogDbController.remove(mCaseId);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }
}
