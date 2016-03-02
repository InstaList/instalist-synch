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

import org.noorganization.instalist.comm.message.RecipeInfo;
import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.model.Recipe;
import org.noorganization.instalist.presenter.IRecipeController;
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
import org.noorganization.instalistsynch.events.RecipeSynchFromNetworkFinished;
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
 * Created by Desnoo on 27.02.2016.
 */
public class RecipeSynch implements ISynch {
    private static final String TAG = "RecipeSynch";

    private ISessionController mSessionController;
    private IRecipeController mRecipeController;
    private IModelMappingDbController mRecipeMappingController;
    //private IModelMappingDbController mCategoryModelMappingController;

    private IClientLogDbController mClientLogDbController;
    private IGroupAuthDbController mGroupAuthDbController;
    private INetworkController<RecipeInfo> mRecipeInfoNetworkController;
    private ITaskErrorLogDbController mTaskErrorLogDbController;

    private eModelType mModelType;
    private EventBus mEventBus;

    public RecipeSynch(eModelType _type) {
        mModelType = _type;

        Context context = GlobalObjects.getInstance().getApplicationContext();
        mSessionController = InMemorySessionController.getInstance();
        mRecipeController = ControllerFactory.getRecipeController(context);
        mRecipeMappingController = ModelMappingDbFactory.getInstance().getSqliteRecipeMappingController();
        mClientLogDbController = LocalSqliteDbControllerFactory.getClientLogController(context);
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(context);
        mRecipeInfoNetworkController = RemoteModelAccessControllerFactory.getInstance().getRecipeNetworkController();
        mTaskErrorLogDbController = TaskErrorLogDbController.getInstance(context);
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void indexLocalEntries(int _groupId) {
        List<ModelMapping> modelMappings = mRecipeMappingController.get(null, null);
        if (modelMappings.size() > 0) {
            return;
        }

        List<Recipe> recipeList = mRecipeController.listAll();
        ModelMapping modelMapping;

        for (Recipe recipe : recipeList) {
            modelMapping =
                    new ModelMapping(null, _groupId, null, recipe.mUUID, new Date(Constants.INITIAL_DATE), new Date(), false);
            mRecipeMappingController.insert(modelMapping);
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

                List<ModelMapping> modelMappingList = mRecipeMappingController.get(
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
                        mRecipeMappingController.insert(modelMapping);
                        break;
                    case UPDATE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        String timeString = logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mRecipeMappingController.update(modelMapping);
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
                        mRecipeMappingController.update(modelMapping);
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
        mRecipeMappingController.insert(modelMapping);
    }

    @Override
    public void removeGroupFromMapping(int _groupId, String _clientUuid) {
        List<ModelMapping> modelMappingList = mRecipeMappingController.get(
                ModelMapping.COLUMN.GROUP_ID
                        + " = ? AND " + ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?",
                new String[]{String.valueOf(_groupId), _clientUuid});
        if (modelMappingList.size() == 0) {
            return;
        }
        mRecipeMappingController.delete(modelMappingList.get(0));
    }

    @Override
    public void synchLocalToNetwork(int _groupId, Date _lastUpdate) {
        String lastUpdateString = ISO8601Utils.format(_lastUpdate, false, TimeZone.getTimeZone("GMT+0000"));
        String authToken = mSessionController.getToken(_groupId);

        if (authToken == null) {
            // todo do some caching of this action
            return;
        }

        List<ModelMapping> modelMappingList = mRecipeMappingController.get(
                ModelMapping.COLUMN.LAST_CLIENT_CHANGE + " >= ? ", new String[]{lastUpdateString});
        for (ModelMapping modelMapping : modelMappingList) {
            if (modelMapping.isDeleted()) {
                // delete the item
                mRecipeInfoNetworkController.deleteItem(new DeleteResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, modelMapping.getServerSideUUID(), authToken);
            } else if (modelMapping.getServerSideUUID() == null) {
                // insert new
                RecipeInfo recipeInfo = new RecipeInfo();
                Recipe recipe = mRecipeController.findById(modelMapping.getClientSideUUID());
                if (recipe == null) {
                    continue;
                }
                String uuid = mRecipeMappingController.generateUuid();
                recipeInfo.setUUID(uuid);
                recipeInfo.setName(recipe.mName);
                Date lastChanged = new Date(modelMapping.getLastClientChange().getTime() - Constants.NETWORK_OFFSET);
                recipeInfo.setLastChanged(lastChanged);
                recipeInfo.setDeleted(false);
                mRecipeInfoNetworkController.createItem(new InsertResponse(modelMapping, uuid), _groupId, recipeInfo, authToken);
            } else {
                // update existing
                RecipeInfo recipeInfo = new RecipeInfo();
                Recipe recipe = mRecipeController.findById(modelMapping.getClientSideUUID());
                if (recipe == null) {
                    // probably the item was deleted
                    mRecipeInfoNetworkController.deleteItem(new DeleteResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, modelMapping.getServerSideUUID(), authToken);
                    continue;
                }
                recipeInfo.setUUID(modelMapping.getServerSideUUID());
                recipeInfo.setName(recipe.mName);
                Date lastChanged = new Date(modelMapping.getLastClientChange().getTime() - Constants.NETWORK_OFFSET);
                recipeInfo.setLastChanged(lastChanged);
                recipeInfo.setDeleted(false);
                mRecipeInfoNetworkController.updateItem(new UpdateResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, recipeInfo.getUUID(), recipeInfo, authToken);
            }
        }
    }


    @Override
    public void synchNetworkToLocal(int _groupId, Date _sinceTime) {
        String authToken = mSessionController.getToken(_groupId);
        if (authToken == null) {
            return;
        }
        mRecipeInfoNetworkController.getList(new GetListResponse(_groupId, _sinceTime), _groupId, ISO8601Utils.format(_sinceTime, false, TimeZone.getTimeZone("GMT+0000")).concat("+0000"), authToken);
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

        mRecipeInfoNetworkController.getItem(new GetItemConflictResolveResponse(_resolveAction, _conflictId, log.getGroupId()), log.getGroupId(), log.getUUID(), authToken);
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
            mRecipeMappingController.delete(mModelMapping);
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
            mRecipeMappingController.update(mModelMapping);
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
            mRecipeMappingController.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class GetListResponse implements IAuthorizedCallbackCompleted<List<RecipeInfo>> {

        private int mGroupId;
        private Date mLastUpdateDate;

        public GetListResponse(int _groupId, Date _lastUpdateDate) {
            mGroupId = _groupId;
            mLastUpdateDate = _lastUpdateDate;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new RecipeSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onCompleted(List<RecipeInfo> _next) {
            for (RecipeInfo recipeInfo : _next) {
                List<ModelMapping> modelMappingList = mRecipeMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), recipeInfo.getUUID()});

                if (modelMappingList.size() == 0) {
                    // new entry
                    Recipe newRecipe = mRecipeController.createRecipe(recipeInfo.getName());
                    if (newRecipe == null) {
                        // TODO some error happened
                        continue;
                    }
                    ModelMapping modelMapping = new ModelMapping(null, mGroupId, recipeInfo.getUUID(),
                            newRecipe.mUUID, recipeInfo.getLastChanged(), recipeInfo.getLastChanged(), false);
                    mRecipeMappingController.insert(modelMapping);
                } else {
                    // entry exists local
                    ModelMapping modelMapping = modelMappingList.get(0);
                    Recipe recipe = mRecipeController.findById(modelMapping.getClientSideUUID());

                    if (recipeInfo.getDeleted()) {
                        // was deleted on server side
                        mRecipeController.removeRecipe(recipe);
                        mRecipeMappingController.delete(modelMapping);
                        continue;
                    }

                    // else there was an update!
                    if (modelMapping.getLastClientChange().after(recipeInfo.getLastChanged())) {
                        // use server side or client side, let the user decide
                        mTaskErrorLogDbController.insert(recipeInfo.getUUID(), mModelType.ordinal(), ITask.ReturnCodes.MERGE_CONFLICT, mGroupId);
                        continue;
                    }

                    Recipe renamedRecipe = mRecipeController.renameRecipe(recipe, recipeInfo.getName());
                    modelMapping.setLastServerChanged(recipeInfo.getLastChanged());
                    mRecipeMappingController.update(modelMapping);
                }
            }

            EventBus.getDefault().post(new RecipeSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new RecipeSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }
    }

    private class GetItemConflictResolveResponse implements IAuthorizedCallbackCompleted<RecipeInfo> {
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
        public void onCompleted(RecipeInfo _next) {
            if (mResolveAction == ITask.ResolveCodes.RESOLVE_USE_CLIENT_SIDE) {
                // use client side
                // no further action needed?
            } else {
                // use server side
                List<ModelMapping> modelMappingList = mRecipeMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), _next.getUUID()});
                if (modelMappingList.size() == 0) {
                    return;
                }

                ModelMapping modelMapping = modelMappingList.get(0);
                Recipe recipe = mRecipeController.findById(modelMapping.getClientSideUUID());
                Recipe renamedRecipe = mRecipeController.renameRecipe(recipe, _next.getName());

                modelMapping.setLastServerChanged(_next.getLastChanged());

                mRecipeMappingController.update(modelMapping);
            }
            mTaskErrorLogDbController.remove(mCaseId);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }
}
