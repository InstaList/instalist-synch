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

package org.noorganization.instalistsynch.controller.synch.impl;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.CategoryInfo;
import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;
import org.noorganization.instalistsynch.controller.local.dba.IClientLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
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
import org.noorganization.instalistsynch.model.GroupAccess;
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
 * The synchronization for the categories.
 * Created by Desnoo on 24.02.2016.
 */
public class CategorySynch implements ISynch {
    private static final String TAG = "CategorySynch";

    private ISessionController mSessionController;
    private ICategoryController mCategoryController;
    private IModelMappingDbController mCategoryModelMappingController;
    private IClientLogDbController mClientLogDbController;
    private IGroupAuthDbController mGroupAuthDbController;
    private INetworkController<CategoryInfo> mCategoryInfoNetworkController;
    private ITaskErrorLogDbController mTaskErrorLogDbController;

    private eModelType mModelType;
    private EventBus mEventBus;

    public CategorySynch(eModelType _type) {
        mModelType = _type;

        Context context = GlobalObjects.getInstance().getApplicationContext();
        mSessionController = InMemorySessionController.getInstance();
        mCategoryController = ControllerFactory.getCategoryController(context);
        mCategoryModelMappingController =
                ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        mClientLogDbController = LocalSqliteDbControllerFactory.getClientLogController(context);
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(context);
        mCategoryInfoNetworkController = RemoteModelAccessControllerFactory.getInstance().getCategoryNetworkController();
        mTaskErrorLogDbController = TaskErrorLogDbController.getInstance(context);
        mEventBus = EventBus.getDefault();
    }

    public CategorySynch(eModelType _type, IModelMappingDbController _modelMappingDbController) {
        mModelType = _type;

        Context context = GlobalObjects.getInstance().getApplicationContext();
        mSessionController = InMemorySessionController.getInstance();
        mCategoryController = ControllerFactory.getCategoryController(context);
        mCategoryModelMappingController =
                ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        mClientLogDbController = LocalSqliteDbControllerFactory.getClientLogController(context);
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(context);
        mCategoryInfoNetworkController = RemoteModelAccessControllerFactory.getInstance().getCategoryNetworkController();
        mTaskErrorLogDbController = TaskErrorLogDbController.getInstance(context);
    }

    @Override
    public void indexLocalEntries(int _groupId) {
        List<ModelMapping> modelMappings = mCategoryModelMappingController.get(null, null);
        if (modelMappings.size() > 0) {
            return;
        }

        List<Category> categoryList = mCategoryController.getAllCategories();
        ModelMapping categoryMapping;

        for (Category category : categoryList) {
            categoryMapping =
                    new ModelMapping(null, _groupId, null, category.mUUID, new Date(Constants.INITIAL_DATE), new Date(), false);
            mCategoryModelMappingController.insert(categoryMapping);
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
        Cursor categoryLogCursor =
                mClientLogDbController.getLogsSince(lastIndexTime, mModelType);
        if (categoryLogCursor.getCount() == 0) {
            categoryLogCursor.close();
            return;
        }

        try {
            while (categoryLogCursor.moveToNext()) {
                // fetch the action type
                int actionId = categoryLogCursor.getInt(categoryLogCursor.getColumnIndex(LogInfo.COLUMN.ACTION));
                eActionType actionType = eActionType.getTypeById(actionId);

                List<ModelMapping> modelMappingList = mCategoryModelMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND " +
                                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?", new String[]{
                                String.valueOf(_groupId),
                                categoryLogCursor.getString(categoryLogCursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID))});
                ModelMapping modelMapping =
                        modelMappingList.size() == 0 ? null : modelMappingList.get(0);

                switch (actionType) {
                    case INSERT:
                        // skip insertion because this should be decided by the user if the non local groups should have access to the category
                        // and also skip if a mapping for this case already exists!
                        if (!isLocal || modelMapping != null) {
                            continue;
                        }

                        String clientUuid = categoryLogCursor.getString(categoryLogCursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID));
                        Date clientDate = ISO8601Utils.parse(categoryLogCursor.getString(categoryLogCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE)), new ParsePosition(0));
                        modelMapping = new ModelMapping(null, groupAuth.getGroupId(), null, clientUuid, new Date(Constants.INITIAL_DATE), clientDate, false);
                        mCategoryModelMappingController.insert(modelMapping);
                        break;
                    case UPDATE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        String timeString = categoryLogCursor.getString(categoryLogCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mCategoryModelMappingController.update(modelMapping);
                        break;
                    case DELETE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        modelMapping.setDeleted(true);
                        timeString = categoryLogCursor.getString(categoryLogCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mCategoryModelMappingController.update(modelMapping);
                        break;
                    default:
                }

            }
        } catch (Exception e) {
            categoryLogCursor.close();
        }
    }

    @Override
    public void addGroupToMapping(int _groupId, String _clientUuid) {
        Date lastUpdate = mClientLogDbController.getLeastRecentUpdateTimeForUuid(_clientUuid);
        if (lastUpdate == null) {
            return;
        }
        ModelMapping modelMapping = new ModelMapping(null, _groupId, null, _clientUuid, new Date(Constants.INITIAL_DATE), lastUpdate, false);
        mCategoryModelMappingController.insert(modelMapping);
    }

    @Override
    public void removeGroupFromMapping(int _groupId, String _clientUuid) {
        List<ModelMapping> modelMappingList = mCategoryModelMappingController.get(
                ModelMapping.COLUMN.GROUP_ID
                        + " = ? AND " + ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?",
                new String[]{String.valueOf(_groupId), _clientUuid});
        if (modelMappingList.size() == 0) {
            return;
        }
        mCategoryModelMappingController.delete(modelMappingList.get(0));
    }

    @Override
    public void synchLocalToNetwork(int _groupId, Date _lastUpdate) {
        String lastUpdateString = ISO8601Utils.format(_lastUpdate, false, TimeZone.getTimeZone("GMT+0000"));
        String authToken = mSessionController.getToken(_groupId);

        if (authToken == null) {
            // todo do some caching of this action
            return;
        }

        List<ModelMapping> categoryMappingList = mCategoryModelMappingController.get(
                ModelMapping.COLUMN.LAST_CLIENT_CHANGE + " >= ? ", new String[]{lastUpdateString});
        for (ModelMapping categoryMapping : categoryMappingList) {
            if (categoryMapping.isDeleted()) {
                // delete the item
                mCategoryInfoNetworkController.deleteItem(new DeleteResponse(categoryMapping, categoryMapping.getServerSideUUID()), _groupId, categoryMapping.getServerSideUUID(), authToken);
            } else if (categoryMapping.getServerSideUUID() == null) {
                // insert new
                CategoryInfo categoryInfo = new CategoryInfo();
                Category category = mCategoryController.getCategoryByID(categoryMapping.getClientSideUUID());
                if (category == null) {
                    continue;
                }
                String uuid = mCategoryModelMappingController.generateUuid();
                categoryInfo.setUUID(uuid);
                categoryInfo.setName(category.mName);
                Date lastChanged = new Date(categoryMapping.getLastClientChange().getTime() - Constants.NETWORK_OFFSET);
                categoryInfo.setLastChanged(lastChanged);
                categoryInfo.setDeleted(false);
                mCategoryInfoNetworkController.createItem(new InsertResponse(categoryMapping, uuid), _groupId, categoryInfo, authToken);
            } else {
                // update existing
                CategoryInfo categoryInfo = new CategoryInfo();
                Category category = mCategoryController.getCategoryByID(categoryMapping.getClientSideUUID());
                if (category == null) {
                    // probably the category was deleted
                    // delete the item
                    mCategoryInfoNetworkController.deleteItem(new DeleteResponse(categoryMapping, categoryMapping.getServerSideUUID()), _groupId, categoryMapping.getServerSideUUID(), authToken);
                    continue;
                }
                categoryInfo.setUUID(categoryMapping.getServerSideUUID());
                categoryInfo.setName(category.mName);
                Date lastChanged = new Date(categoryMapping.getLastClientChange().getTime() - Constants.NETWORK_OFFSET);
                categoryInfo.setLastChanged(lastChanged);
                categoryInfo.setDeleted(false);
                mCategoryInfoNetworkController.updateItem(new UpdateResponse(categoryMapping, categoryMapping.getServerSideUUID()), _groupId, categoryInfo.getUUID(), categoryInfo, authToken);
            }
        }
    }


    @Override
    public void synchNetworkToLocal(int _groupId, Date _sinceTime) {
        String authToken = mSessionController.getToken(_groupId);
        if (authToken == null) {
            return;
        }
        mCategoryInfoNetworkController.getList(new GetListResponse(_groupId, _sinceTime), _groupId, ISO8601Utils.format(_sinceTime, false, TimeZone.getTimeZone("GMT+0000")).concat("+0000"), authToken);
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

        mCategoryInfoNetworkController.getItem(new GetItemConflictResolveResponse(_resolveAction, _conflictId, log.getGroupId()), log.getGroupId(), log.getUUID(), authToken);
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
            mCategoryModelMappingController.delete(mModelMapping);
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
            mCategoryModelMappingController.update(mModelMapping);
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
            mCategoryModelMappingController.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class GetListResponse implements IAuthorizedCallbackCompleted<List<CategoryInfo>> {

        private int mGroupId;
        private Date mLastUpdateDate;

        public GetListResponse(int _groupId, Date _lastUpdateDate) {
            mGroupId = _groupId;
            mLastUpdateDate = _lastUpdateDate;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new CategorySynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onCompleted(List<CategoryInfo> _next) {
            for (CategoryInfo categoryInfo : _next) {
                List<ModelMapping> modelMappingList = mCategoryModelMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), categoryInfo.getUUID()});

                if (modelMappingList.size() == 0) {
                    // new entry
                    Category newCategory = mCategoryController.createCategory(categoryInfo.getName());
                    if (newCategory == null) {
                        // TODO some error happened
                        continue;
                    }
                    ModelMapping modelMapping = new ModelMapping(null, mGroupId, categoryInfo.getUUID(),
                            newCategory.mUUID, categoryInfo.getLastChanged(), categoryInfo.getLastChanged(), false);
                    mCategoryModelMappingController.insert(modelMapping);
                } else {
                    // entry exists local
                    ModelMapping modelMapping = modelMappingList.get(0);
                    Category category = mCategoryController.getCategoryByID(modelMapping.getClientSideUUID());

                    if (categoryInfo.getDeleted()) {
                        // was deleted on server side
                        mCategoryController.removeCategory(category);
                        mCategoryModelMappingController.delete(modelMapping);
                        continue;
                    }

                    // else there was an update!
                    if (modelMapping.getLastClientChange().after(categoryInfo.getLastChanged())) {
                        // use server side or client side, let the user decide
                        mTaskErrorLogDbController.insert(categoryInfo.getUUID(), mModelType.ordinal(), ITask.ReturnCodes.MERGE_CONFLICT, mGroupId);
                        continue;
                    }

                    Category renamedCategory = mCategoryController.renameCategory(category, categoryInfo.getName());
                    modelMapping.setLastServerChanged(categoryInfo.getLastChanged());
                    /*
                    if (!renamedCategory.equals(category)) {
                        // todo give it another name?
                        continue;
                    }*/
                    mCategoryModelMappingController.update(modelMapping);
                }
            }
            IGroupAuthAccessDbController groupAuthAccessDbController = LocalSqliteDbControllerFactory.getAuthAccessDbController(GlobalObjects.getInstance().getApplicationContext());
            GroupAccess access = groupAuthAccessDbController.getGroupAuthAccess(mGroupId);
            access.setLastUpdateFromServer(new Date());
            groupAuthAccessDbController.update(access);
            EventBus.getDefault().post(new CategorySynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new CategorySynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }
    }

    private class GetItemConflictResolveResponse implements IAuthorizedCallbackCompleted<CategoryInfo> {
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
        public void onCompleted(CategoryInfo _next) {
            if (mResolveAction == ITask.ResolveCodes.RESOLVE_USE_CLIENT_SIDE) {
                // use client side
                // no further action needed?
            } else {
                // use server side
                List<ModelMapping> modelMappingList = mCategoryModelMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), _next.getUUID()});
                if (modelMappingList.size() == 0) {
                    return;
                }

                ModelMapping modelMapping = modelMappingList.get(0);

                Category category = mCategoryController.getCategoryByID(modelMapping.getClientSideUUID());

                Category renamedCategory = mCategoryController.renameCategory(category, _next.getName());
                modelMapping.setLastServerChanged(_next.getLastChanged());

                if (!renamedCategory.equals(category)) {
                    // todo give it another name?
                    return;
                }
                mCategoryModelMappingController.update(modelMapping);
            }
            mTaskErrorLogDbController.remove(mCaseId);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }
}
