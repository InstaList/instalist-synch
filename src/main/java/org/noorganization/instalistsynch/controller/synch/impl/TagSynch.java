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

import org.noorganization.instalist.comm.message.TagInfo;
import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.model.Tag;
import org.noorganization.instalist.presenter.ITagController;
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
import org.noorganization.instalistsynch.events.TagSynchFromNetworkFinished;
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
 * The synchronization for the tags.
 * Created by Desnoo on 24.02.2016.
 */
public class TagSynch implements ISynch {
    private static final String TAG = "TagSynch";

    private ISessionController mSessionController;
    private ITagController mTagController;
    private IModelMappingDbController mTagModelMappingController;
    private IClientLogDbController mClientLogDbController;
    private IGroupAuthDbController mGroupAuthDbController;
    private INetworkController<TagInfo> mTagNetworkController;
    private ITaskErrorLogDbController mTaskErrorLogDbController;

    private eModelType mModelType;
    private EventBus mEventBus;

    public TagSynch(eModelType _type) {
        mModelType = _type;

        Context context = GlobalObjects.getInstance().getApplicationContext();
        mSessionController = InMemorySessionController.getInstance();
        mTagController = ControllerFactory.getTagController(context);
        mTagModelMappingController =
                ModelMappingDbFactory.getInstance().getSqliteTagMappingController();
        mClientLogDbController = LocalSqliteDbControllerFactory.getClientLogController(context);
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(context);
        mTagNetworkController = RemoteModelAccessControllerFactory.getInstance().getTagNetworkController();
        mTaskErrorLogDbController = TaskErrorLogDbController.getInstance(context);
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void indexLocalEntries(int _groupId) {
        List<ModelMapping> modelMappings = mTagModelMappingController.get(null, null);
        if (modelMappings.size() > 0) {
            return;
        }

        List<Tag> tagList = mTagController.listAll();
        ModelMapping tagMapping;

        for (Tag tag : tagList) {
            tagMapping =
                    new ModelMapping(null, _groupId, null, tag.mUUID, new Date(Constants.INITIAL_DATE), new Date(), false);
            mTagModelMappingController.insert(tagMapping);
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
        Cursor tagLogCursor =
                mClientLogDbController.getLogsSince(lastIndexTime, mModelType);
        if (tagLogCursor.getCount() == 0) {
            tagLogCursor.close();
            return;
        }

        try {
            while (tagLogCursor.moveToNext()) {
                // fetch the action type
                int actionId = tagLogCursor.getInt(tagLogCursor.getColumnIndex(LogInfo.COLUMN.ACTION));
                eActionType actionType = eActionType.getTypeById(actionId);

                List<ModelMapping> modelMappingList = mTagModelMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND " +
                                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?", new String[]{
                                String.valueOf(_groupId),
                                tagLogCursor.getString(tagLogCursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID))});
                ModelMapping modelMapping =
                        modelMappingList.size() == 0 ? null : modelMappingList.get(0);

                switch (actionType) {
                    case INSERT:
                        // skip insertion because this should be decided by the user if the non local groups should have access to the category
                        // and also skip if a mapping for this case already exists!
                        if (!isLocal || modelMapping != null) {
                            continue;
                        }

                        String clientUuid = tagLogCursor.getString(tagLogCursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID));
                        Date clientDate = ISO8601Utils.parse(tagLogCursor.getString(tagLogCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE)), new ParsePosition(0));
                        modelMapping = new ModelMapping(null, groupAuth.getGroupId(), null, clientUuid, new Date(Constants.INITIAL_DATE), clientDate, false);
                        mTagModelMappingController.insert(modelMapping);
                        break;
                    case UPDATE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        String timeString = tagLogCursor.getString(tagLogCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mTagModelMappingController.update(modelMapping);
                        break;
                    case DELETE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        modelMapping.setDeleted(true);
                        timeString = tagLogCursor.getString(tagLogCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mTagModelMappingController.update(modelMapping);
                        break;
                    default:
                }

            }
        } catch (Exception e) {
            tagLogCursor.close();
        }
    }

    @Override
    public void addGroupToMapping(int _groupId, String _clientUuid) {
        Date lastUpdate = mClientLogDbController.getLeastRecentUpdateTimeForUuid(_clientUuid);
        if (lastUpdate == null) {
            return;
        }
        ModelMapping modelMapping = new ModelMapping(null, _groupId, null, _clientUuid, new Date(Constants.INITIAL_DATE), lastUpdate, false);
        mTagModelMappingController.insert(modelMapping);
    }

    @Override
    public void removeGroupFromMapping(int _groupId, String _clientUuid) {
        List<ModelMapping> modelMappingList = mTagModelMappingController.get(
                ModelMapping.COLUMN.GROUP_ID
                        + " = ? AND " + ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?",
                new String[]{String.valueOf(_groupId), _clientUuid});
        if (modelMappingList.size() == 0) {
            return;
        }
        mTagModelMappingController.delete(modelMappingList.get(0));
    }

    @Override
    public void synchLocalToNetwork(int _groupId, Date _lastUpdate) {
        String lastUpdateString = ISO8601Utils.format(_lastUpdate, false, TimeZone.getTimeZone("GMT+0000"));
        String authToken = mSessionController.getToken(_groupId);

        if (authToken == null) {
            // todo do some caching of this action
            return;
        }

        List<ModelMapping> tagModelMappingList = mTagModelMappingController.get(
                ModelMapping.COLUMN.LAST_CLIENT_CHANGE + " >= ? ", new String[]{lastUpdateString});
        for (ModelMapping tagMapping : tagModelMappingList) {
            if(tagMapping.getClientSideUUID() == null)
                continue;
            if (tagMapping.isDeleted()) {
                // delete the item
                mTagNetworkController.deleteItem(new DeleteResponse(tagMapping, tagMapping.getServerSideUUID()), _groupId, tagMapping.getServerSideUUID(), authToken);
            } else if (tagMapping.getServerSideUUID() == null) {
                // insert new
                TagInfo tagInfo = new TagInfo();
                Tag tag = mTagController.findById(tagMapping.getClientSideUUID());
                if (tag == null) {
                    continue;
                }
                String uuid = mTagModelMappingController.generateUuid();
                tagInfo.setUUID(uuid);
                tagInfo.setName(tag.mName);
                Date lastChanged = new Date(tagMapping.getLastClientChange().getTime() - Constants.NETWORK_OFFSET);
                tagInfo.setLastChanged(lastChanged);
                tagInfo.setDeleted(false);
                mTagNetworkController.createItem(new InsertResponse(tagMapping, uuid), _groupId, tagInfo, authToken);
            } else {
                // update existing
                TagInfo tagInfo = new TagInfo();
                Tag tag = mTagController.findById(tagMapping.getClientSideUUID());
                if (tag == null) {
                    // probably the category was deleted
                    // delete the item
                    mTagNetworkController.deleteItem(new DeleteResponse(tagMapping, tagMapping.getServerSideUUID()), _groupId, tagMapping.getServerSideUUID(), authToken);
                    continue;
                }
                tagInfo.setUUID(tagMapping.getServerSideUUID());
                tagInfo.setName(tag.mName);
                Date lastChanged = new Date(tagMapping.getLastClientChange().getTime()-Constants.NETWORK_OFFSET);

                tagInfo.setLastChanged(lastChanged);
                tagInfo.setDeleted(false);
                mTagNetworkController.updateItem(new UpdateResponse(tagMapping, tagMapping.getServerSideUUID()), _groupId, tagInfo.getUUID(), tagInfo, authToken);
            }
        }
    }


    @Override
    public void synchNetworkToLocal(int _groupId, Date _sinceTime) {
        String authToken = mSessionController.getToken(_groupId);
        if (authToken == null) {
            return;
        }
        mTagNetworkController.getList(new GetListResponse(_groupId, _sinceTime), _groupId, ISO8601Utils.format(_sinceTime, false, TimeZone.getTimeZone("GMT+0000")).concat("+0000"), authToken);
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

        mTagNetworkController.getItem(new GetItemConflictResolveResponse(_resolveAction, _conflictId, log.getGroupId()), log.getGroupId(), log.getUUID(), authToken);
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
            mTagModelMappingController.delete(mModelMapping);
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
            mTagModelMappingController.update(mModelMapping);
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
            mTagModelMappingController.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class GetListResponse implements IAuthorizedCallbackCompleted<List<TagInfo>> {

        private int mGroupId;
        private Date mLastUpdateDate;

        public GetListResponse(int _groupId, Date _lastUpdateDate) {
            mGroupId = _groupId;
            mLastUpdateDate = _lastUpdateDate;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new TagSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onCompleted(List<TagInfo> _next) {
            for (TagInfo tagInfo : _next) {
                List<ModelMapping> modelMappingList = mTagModelMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), tagInfo.getUUID()});

                if (modelMappingList.size() == 0) {
                    // new entry
                    Tag newTag = mTagController.createTag(tagInfo.getName());
                    if (newTag == null) {
                        // TODO some error happenedd
                        Log.e(TAG, "onCompleted: cannot create new tag with name " + tagInfo.getName());
                        continue;
                    }
                    ModelMapping modelMapping = new ModelMapping(null, mGroupId, tagInfo.getUUID(),
                            newTag.mUUID, tagInfo.getLastChanged(), tagInfo.getLastChanged(), false);
                    mTagModelMappingController.insert(modelMapping);
                } else {
                    // entry exists local
                    ModelMapping modelMapping = modelMappingList.get(0);
                    Tag tag = mTagController.findById(modelMapping.getClientSideUUID());

                    if (tagInfo.getDeleted()) {
                        // was deleted on server side
                        mTagController.removeTag(tag);
                        mTagModelMappingController.delete(modelMapping);
                        continue;
                    }

                    // else there was an update!
                    if (modelMapping.getLastClientChange().after(tagInfo.getLastChanged())) {
                        // use server side or client side, let the user decide
                        mTaskErrorLogDbController.insert(tagInfo.getUUID(), mModelType.ordinal(), ITask.ReturnCodes.MERGE_CONFLICT, mGroupId);
                        continue;
                    }

                    Tag renamedTag = mTagController.renameTag(tag, tagInfo.getName());
                    modelMapping.setLastServerChanged(tagInfo.getLastChanged());
                    /*
                    if (!renamedCategory.equals(category)) {
                        // todo give it another name?
                        continue;
                    }*/
                    mTagModelMappingController.update(modelMapping);
                }
            }
            IGroupAuthAccessDbController groupAuthAccessDbController = LocalSqliteDbControllerFactory.getAuthAccessDbController(GlobalObjects.getInstance().getApplicationContext());
            GroupAccess access = groupAuthAccessDbController.getGroupAuthAccess(mGroupId);
            access.setLastUpdateFromServer(new Date());
            groupAuthAccessDbController.update(access);
            EventBus.getDefault().post(new TagSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new TagSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }
    }

    private class GetItemConflictResolveResponse implements IAuthorizedCallbackCompleted<TagInfo> {
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
        public void onCompleted(TagInfo _next) {
            if (mResolveAction == ITask.ResolveCodes.RESOLVE_USE_CLIENT_SIDE) {
                // use client side
                // no further action needed?
            } else {
                // use server side
                List<ModelMapping> modelMappingList = mTagModelMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), _next.getUUID()});
                if (modelMappingList.size() == 0) {
                    return;
                }

                ModelMapping modelMapping = modelMappingList.get(0);

                Tag tag = mTagController.findById(modelMapping.getClientSideUUID());

                Tag renamedTag = mTagController.renameTag(tag, _next.getName());
                modelMapping.setLastServerChanged(_next.getLastChanged());

                if (!renamedTag.equals(tag)) {
                    // todo give it another name?
                    return;
                }
                mTagModelMappingController.update(modelMapping);
            }
            mTaskErrorLogDbController.remove(mCaseId);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }
}
