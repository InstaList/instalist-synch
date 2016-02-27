package org.noorganization.instalistsynch.controller.synch.impl;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.IListController;
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
import org.noorganization.instalistsynch.events.CategorySynchFromNetworkFinished;
import org.noorganization.instalistsynch.events.ListSynchFromNetworkFinished;
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
public class ListSynch implements ISynch {
    private static final String TAG = "ListSynch";

    private ISessionController mSessionController;
    private IListController mListController;
    private IModelMappingDbController mListModelMappingController;
    private IClientLogDbController mClientLogDbController;
    private IGroupAuthDbController mGroupAuthDbController;
    private INetworkController<ListInfo> mListInfoNetworkController;
    private ITaskErrorLogDbController mTaskErrorLogDbController;

    private eModelType mModelType;
    private EventBus mEventBus;

    public ListSynch(eModelType _type) {
        mModelType = _type;

        Context context = GlobalObjects.getInstance().getApplicationContext();
        mSessionController = InMemorySessionController.getInstance();
        mListController = ControllerFactory.getListController(context);
        mListModelMappingController =
                ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        mClientLogDbController = LocalSqliteDbControllerFactory.getClientLogController(context);
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(context);
        mListInfoNetworkController = RemoteModelAccessControllerFactory.getInstance().getListNetworkController();
        mTaskErrorLogDbController = TaskErrorLogDbController.getInstance(context);
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void indexLocalEntries(int _groupId) {
        List<ModelMapping> modelMappings = mListModelMappingController.get(null, null);
        if (modelMappings.size() > 0) {
            return;
        }

        List<ShoppingList> listList = mListController.getAllLists();
        ModelMapping       modelMapping;

        for (ShoppingList list : listList) {
            modelMapping =
                    new ModelMapping(null, _groupId, null, list.mUUID, new Date(Constants.INITIAL_DATE), new Date(), false);
            mListModelMappingController.insert(modelMapping);
        }
    }

    @Override
    public void indexLocal(int _groupId, Date _lastIndexTime) {
        String    lastIndexTime = ISO8601Utils.format(_lastIndexTime, false, TimeZone.getTimeZone("GMT+0000"));//.concat("+0000");
        boolean   isLocal       = false;
        GroupAuth groupAuth     = mGroupAuthDbController.getLocalGroup();
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

                List<ModelMapping> modelMappingList = mListModelMappingController.get(
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
                        mListModelMappingController.insert(modelMapping);
                        break;
                    case UPDATE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        String timeString = logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mListModelMappingController.update(modelMapping);
                        break;
                    case DELETE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        modelMapping.setDeleted(true);
                        mListModelMappingController.update(modelMapping);
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
        mListModelMappingController.insert(modelMapping);
    }

    @Override
    public void removeGroupFromMapping(int _groupId, String _clientUuid) {
        List<ModelMapping> modelMappingList = mListModelMappingController.get(
                ModelMapping.COLUMN.GROUP_ID
                        + " = ? AND " + ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?",
                new String[]{String.valueOf(_groupId), _clientUuid});
        if (modelMappingList.size() == 0) {
            return;
        }
        mListModelMappingController.delete(modelMappingList.get(0));
    }

    @Override
    public void synchLocalToNetwork(int _groupId, Date _lastUpdate) {
        String lastUpdateString = ISO8601Utils.format(_lastUpdate, false, TimeZone.getTimeZone("GMT+0000"));
        String authToken        = mSessionController.getToken(_groupId);

        if (authToken == null) {
            // todo do some caching of this action
            return;
        }

        List<ModelMapping> modelMappingList = mListModelMappingController.get(
                ModelMapping.COLUMN.LAST_CLIENT_CHANGE + " >= ? ", new String[]{lastUpdateString});
        for (ModelMapping modelMapping : modelMappingList) {
            if (modelMapping.isDeleted()) {
                // delete the item
                mListInfoNetworkController.deleteItem(new DeleteResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, modelMapping.getServerSideUUID(), authToken);
            } else if (modelMapping.getServerSideUUID() == null) {
                // insert new
                ListInfo listInfo = new ListInfo();
                ShoppingList list = mListController.getListById(modelMapping.getClientSideUUID());
                if (list == null) {
                    continue;
                }
                String uuid = mListModelMappingController.generateUuid();
                listInfo.setUUID(uuid);
                listInfo.setName(list.mName);
                listInfo.setCategoryUUID(list.mCategory == null ? null : list.mCategory.mUUID);
                listInfo.setLastChanged(modelMapping.getLastClientChange());
                listInfo.setDeleted(false);
                mListInfoNetworkController.createItem(new InsertResponse(modelMapping, uuid), _groupId, listInfo, authToken);
            } else {
                // update existing
                ListInfo listInfo = new ListInfo();
                ShoppingList list = mListController.getListById(modelMapping.getClientSideUUID());
                if (list == null) {
                    continue;
                }
                listInfo.setUUID(modelMapping.getServerSideUUID());
                listInfo.setName(list.mName);
                listInfo.setCategoryUUID(list.mCategory == null ? null : list.mCategory.mUUID);
                listInfo.setLastChanged(modelMapping.getLastClientChange());
                listInfo.setDeleted(false);
                mListInfoNetworkController.updateItem(new UpdateResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, listInfo.getUUID(), listInfo, authToken);
            }
        }
    }


    @Override
    public void synchNetworkToLocal(int _groupId, Date _sinceTime) {
        String authToken = mSessionController.getToken(_groupId);
        if (authToken == null) {
            return;
        }
        mListInfoNetworkController.getList(new GetListResponse(_groupId, _sinceTime), _groupId, ISO8601Utils.format(_sinceTime, false, TimeZone.getTimeZone("GMT+0000")).concat("+0000"), authToken);
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

        mListInfoNetworkController.getItem(new GetItemConflictResolveResponse(_resolveAction, _conflictId, log.getGroupId()), log.getGroupId(), log.getUUID(), authToken);
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
            mListModelMappingController.delete(mModelMapping);
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
            mListModelMappingController.update(mModelMapping);
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
            mListModelMappingController.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class GetListResponse implements IAuthorizedCallbackCompleted<List<ListInfo>> {

        private int mGroupId;
        private Date mLastUpdateDate;

        public GetListResponse(int _groupId, Date _lastUpdateDate) {
            mGroupId = _groupId;
            mLastUpdateDate = _lastUpdateDate;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new ListSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onCompleted(List<ListInfo> _next) {
            for (ListInfo listInfo : _next) {
                List<ModelMapping> modelMappingList = mListModelMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), listInfo.getUUID()});

                if (modelMappingList.size() == 0) {
                    // new entry
                    ShoppingList newList = mListController.addList(listInfo.getName());
                    if (newList == null) {
                        // TODO some error happened
                        continue;
                    }
                    ModelMapping modelMapping = new ModelMapping(null, mGroupId, listInfo.getUUID(),
                            newList.mUUID, listInfo.getLastChanged(), listInfo.getLastChanged(), false);
                    mListModelMappingController.insert(modelMapping);
                } else {
                    // entry exists local
                    ModelMapping modelMapping = modelMappingList.get(0);
                    ShoppingList list = mListController.getListById(modelMapping.getClientSideUUID());

                    if (listInfo.getDeleted()) {
                        // was deleted on server side
                        mListController.removeList(list);
                        mListModelMappingController.delete(modelMapping);
                        continue;
                    }

                    // else there was an update!
                    if (modelMapping.getLastClientChange().after(listInfo.getLastChanged())) {
                        // use server side or client side, let the user decide
                        mTaskErrorLogDbController.insert(listInfo.getUUID(), mModelType.ordinal(), ITask.ReturnCodes.MERGE_CONFLICT, mGroupId);
                        continue;
                    }

                    ShoppingList renamedList = mListController.renameList(list, listInfo.getName());
                    if (listInfo.getCategoryUUID() != null) {
                        IModelMappingDbController categoryMappingController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
                        List<ModelMapping> categoryMappingList = categoryMappingController.get(
                                ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ? AND "
                                        + ModelMapping.COLUMN.GROUP_ID + " = ?", new String[]{
                                        listInfo.getCategoryUUID(), String.valueOf(mGroupId)});
                        if (categoryMappingList.size() != 0) {
                            ModelMapping categoryMapping = categoryMappingList.get(0);
                            ICategoryController categoryController = ControllerFactory.getCategoryController(GlobalObjects.getInstance().getApplicationContext());
                            Category category = categoryController.getCategoryByID(categoryMapping.getClientSideUUID());
                            // todo category not available yet, check on this condition
                            if (category != null) {
                                ShoppingList newCategoryList = mListController.moveToCategory(renamedList, category);
                                if (newCategoryList == null) {
                                    Log.e(TAG, "onCompleted: the move to a new category failed! Do a rollback");
                                }
                            }
                        }

                    }

                    modelMapping.setLastServerChanged(listInfo.getLastChanged());

                    if (!renamedList.equals(list)) {
                        // todo give it another name?
                        continue;
                    }
                    mListModelMappingController.update(modelMapping);
                }
            }

            EventBus.getDefault().post(new ListSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new ListSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }
    }

    private class GetItemConflictResolveResponse implements IAuthorizedCallbackCompleted<ListInfo> {
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
        public void onCompleted(ListInfo _next) {
            if (mResolveAction == ITask.ResolveCodes.RESOLVE_USE_CLIENT_SIDE) {
                // use client side
                // no further action needed?
            } else {
                // use server side
                List<ModelMapping> modelMappingList = mListModelMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), _next.getUUID()});
                if (modelMappingList.size() == 0) {
                    return;
                }

                ModelMapping modelMapping = modelMappingList.get(0);
                ShoppingList list = mListController.getListById(modelMapping.getClientSideUUID());
                ShoppingList renamedList = mListController.renameList(list, _next.getName());

                if (_next.getCategoryUUID() != null) {
                    IModelMappingDbController categoryMappingController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
                    List<ModelMapping> categoryMappingList = categoryMappingController.get(
                            ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ? AND "
                                    + ModelMapping.COLUMN.GROUP_ID + " = ?", new String[]{
                                    _next.getCategoryUUID(), String.valueOf(mGroupId)});
                    if (categoryMappingList.size() != 0) {
                        ModelMapping categoryMapping = categoryMappingList.get(0);
                        ICategoryController categoryController = ControllerFactory.getCategoryController(GlobalObjects.getInstance().getApplicationContext());
                        Category category = categoryController.getCategoryByID(categoryMapping.getClientSideUUID());
                        // todo category not available yet, check on this condition
                        if (category != null) {
                            ShoppingList newCategoryList = mListController.moveToCategory(renamedList, category);
                            if (newCategoryList == null) {
                                Log.e(TAG, "onCompleted: the move to a new category failed! Do a rollback");
                                mListController.renameList(renamedList, list.mName);
                                return;
                            }
                        }
                    }
                }
                modelMapping.setLastServerChanged(_next.getLastChanged());
                if (!renamedList.equals(list)) {
                    // todo give it another name?
                    return;
                }
                mListModelMappingController.update(modelMapping);
            }
            mTaskErrorLogDbController.remove(mCaseId);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }
}
