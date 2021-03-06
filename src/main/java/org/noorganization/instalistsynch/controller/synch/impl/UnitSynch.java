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

import org.noorganization.instalist.comm.message.UnitInfo;
import org.noorganization.instalist.types.ActionType;
import org.noorganization.instalist.types.ModelType;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.model.Unit;
import org.noorganization.instalist.presenter.IUnitController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;
import org.noorganization.instalistsynch.controller.local.dba.IClientLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.ITaskErrorLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.model.INetworkController;
import org.noorganization.instalistsynch.controller.network.model.RemoteModelAccessControllerFactory;
import org.noorganization.instalistsynch.controller.synch.ISynch;
import org.noorganization.instalistsynch.controller.synch.impl.generic.IInfoConverter;
import org.noorganization.instalistsynch.controller.synch.impl.generic.InfoConverterFactory;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.events.UnitSynchFromNetworkFinished;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.model.TaskErrorLog;
import org.noorganization.instalistsynch.utils.Constants;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;

/**
 * Class for syncing units. Implemented like ListSynch.
 * <p/>
 * Created by Michael Wodniok on 27.02.16.
 */
public class UnitSynch implements ISynch {

    private static final String TAG = UnitSynch.class.getSimpleName();

    private IClientLogDbController mLocalLogController;
    private IModelMappingDbController mMappingController;
    private ISessionController mSessionController;
    private ITaskErrorLogDbController mErrorLogController;
    private INetworkController<UnitInfo> mNetworkController;
    private IUnitController mUnitController;

    public UnitSynch() {
        Context context = GlobalObjects.getInstance().getApplicationContext();
        mMappingController = ModelMappingDbFactory.getInstance().
                getSqliteUnitMappingController();
        mLocalLogController = LocalSqliteDbControllerFactory.getClientLogController(context);
        mSessionController = InMemorySessionController.getInstance();
        mUnitController = ControllerFactory.getUnitController(context);
        mNetworkController = RemoteModelAccessControllerFactory.getInstance().
                getUnitNetworkController();
        mErrorLogController = LocalSqliteDbControllerFactory.getTaskErrorLogDbController(context);
    }

    @Override
    public void indexLocalEntries(int _groupId) {
        List<ModelMapping> existingMappings = mMappingController.get(null, null);
        if (!existingMappings.isEmpty()) {
            return;
        }

        List<Unit> unitList = mUnitController.listAll(Unit.COLUMN.NAME, true);

        for (Unit unit : unitList) {
            if (unit.mUUID.contentEquals("-"))
                continue;

            ModelMapping newMapping = new ModelMapping(null, _groupId, null, unit.mUUID,
                    new Date(Constants.INITIAL_DATE), new Date(), false);
            mMappingController.insert(newMapping);
        }
    }

    @Override
    public void synchLocalToNetwork(int _groupId, Date _lastUpdate) {
        String lastUpdateString = ISO8601Utils.format(_lastUpdate, false, Constants.TIME_ZONE);
        String authToken = mSessionController.getToken(_groupId);

        if (authToken == null) {
            // todo do some caching of this action
            return;
        }

        List<ModelMapping> modelMappingList = mMappingController.get(
                ModelMapping.COLUMN.LAST_CLIENT_CHANGE + " >= ? ", new String[]{lastUpdateString});
        for (ModelMapping modelMapping : modelMappingList) {
            if (modelMapping.isDeleted()) {
                // delete the item
                mNetworkController.deleteItem(
                        new DeletionCallback(modelMapping, modelMapping.getServerSideUUID()),
                        _groupId,
                        modelMapping.getServerSideUUID(),
                        authToken);
            } else if (modelMapping.getServerSideUUID() == null) {
                // insert new
                Unit element = mUnitController.findById(modelMapping.getClientSideUUID());
                if (element == null) {
                    continue;
                }
                String uuid = mMappingController.generateUuid();

                Date lastUpdate = new Date(modelMapping.getLastClientChange().getTime() - Constants.NETWORK_OFFSET);
                IInfoConverter<Unit, UnitInfo> converter =
                        (IInfoConverter<Unit, UnitInfo>) InfoConverterFactory.getConverter(Unit.class);
                UnitInfo elementInfo = converter.toInfo(element, lastUpdate);

                mNetworkController.createItem(new InsertCallback(modelMapping, uuid), _groupId,
                        elementInfo, authToken);
            } else {
                // update existing
                Unit element = mUnitController.findById(modelMapping.getClientSideUUID());
                if (element == null) {
                    continue;
                }
                Date lastChangeDate = new Date(modelMapping.getLastClientChange().getTime() - Constants.NETWORK_OFFSET);
                IInfoConverter<Unit, UnitInfo> converter =
                        (IInfoConverter<Unit, UnitInfo>) InfoConverterFactory.getConverter(Unit.class);
                UnitInfo elementInfo = converter.toInfo(element, lastChangeDate);


                mNetworkController.updateItem(
                        new UpdateCallback(modelMapping, modelMapping.getServerSideUUID()),
                        _groupId, elementInfo.getUUID(), elementInfo, authToken);
            }
        }
    }

    @Override
    public void indexLocal(int _groupId, Date _lastIndexTime) {
        String lastIndexTime = ISO8601Utils.format(_lastIndexTime, false,
                TimeZone.getTimeZone("GMT+0000"));
        Cursor changeLog = mLocalLogController.getLogsSince(lastIndexTime, ModelType.UNIT);

        while (changeLog.moveToNext()) {
            int action = changeLog.getInt(changeLog.getColumnIndex(LogInfo.COLUMN.ACTION));

            String uuid = changeLog.getString(changeLog.getColumnIndex(LogInfo.COLUMN.ITEM_UUID));
            if (uuid.contentEquals("-"))
                continue;

            List<ModelMapping> existingMappings = mMappingController.get(
                    ModelMapping.COLUMN.CLIENT_SIDE_UUID + " = ? AND " +
                            ModelMapping.COLUMN.GROUP_ID + " = ?",
                    new String[]{uuid, String.valueOf(_groupId)});
            ModelMapping existingMapping = (existingMappings.size() == 0 ? null :
                    existingMappings.get(0));

            switch (action) {
                case ActionType.INSERT: {
                    if (existingMapping == null) {
                        ModelMapping newMapping = new ModelMapping(null, _groupId, null, uuid,
                                new Date(Constants.INITIAL_DATE), new Date(), false);
                        mMappingController.insert(newMapping);
                    }
                    break;
                }
                case ActionType.UPDATE: {
                    if (existingMapping == null) {
                        Log.e(TAG, "Changelog contains update, but mapping does not exist. " +
                                "Ignoring.");
                        continue;
                    }
                    try {
                        Date clientDate = ISO8601Utils.parse(
                                changeLog.getString(changeLog.getColumnIndex(LogInfo.COLUMN.
                                        ACTION_DATE)),
                                new ParsePosition(0));
                        existingMapping.setLastClientChange(clientDate);
                        mMappingController.update(existingMapping);
                    } catch (ParseException e) {
                        Log.e(TAG, "Change log contains invalid date: " + e.getMessage());
                        continue;
                    }
                    break;
                }
                case ActionType.DELETE: {
                    if (existingMapping == null) {
                        Log.e(TAG, "Changelog contains deletion, but mapping does not exist. " +
                                "Ignoring.");
                        continue;
                    }
                    try {
                        Date clientDate = ISO8601Utils.parse(
                                changeLog.getString(changeLog.getColumnIndex(LogInfo.COLUMN.
                                        ACTION_DATE)),
                                new ParsePosition(0));
                        existingMapping.setLastClientChange(clientDate);
                        existingMapping.setDeleted(true);
                        mMappingController.update(existingMapping);
                    } catch (ParseException e) {
                        Log.e(TAG, "Change log contains invalid date: " + e.getMessage());
                        continue;
                    }
                    break;
                }
                default:
                    Log.w(TAG, "Changelog contains entry without action.");
                    continue;
            }
        }
        changeLog.close();
    }

    @Override
    public void addGroupToMapping(int _groupId, String _clientUuid) {
        Date lastUpdate = mLocalLogController.getLeastRecentUpdateTimeForUuid(_clientUuid);
        if (lastUpdate == null) {
            return;
        }
        ModelMapping modelMapping = new ModelMapping(null, _groupId, null, _clientUuid,
                new Date(Constants.INITIAL_DATE), lastUpdate, false);
        mMappingController.insert(modelMapping);
    }

    @Override
    public void removeGroupFromMapping(int _groupId, String _clientUuid) {
        List<ModelMapping> modelMappingList = mMappingController.get(
                ModelMapping.COLUMN.GROUP_ID + " = ? AND " +
                        ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?",
                new String[]{String.valueOf(_groupId), _clientUuid});
        if (modelMappingList.size() == 0) {
            return;
        }
        mMappingController.delete(modelMappingList.get(0));
    }

    @Override
    public void synchNetworkToLocal(int _groupId, Date _sinceTime) {
        String authToken = mSessionController.getToken(_groupId);
        if (authToken == null) {
            return;
        }
        String since = ISO8601Utils.format(_sinceTime, false, TimeZone.getTimeZone("GMT+0000")).
                concat("+0000");
        mNetworkController.getList(new ChangedElementsCallback(_groupId, _sinceTime), _groupId,
                since, authToken);
    }

    @Override
    public void resolveConflict(int _conflictId, int _resolveAction) {
        TaskErrorLog log = mErrorLogController.findById(_conflictId);
        if (log == null) {
            return;
        }
        String authToken = mSessionController.getToken(log.getGroupId());
        if (authToken == null) {
            return;
        }

        mNetworkController.getItem(
                new ConflictResolveCallback(_resolveAction, _conflictId, log.getGroupId()),
                log.getGroupId(),
                log.getUUID(),
                authToken);
    }

    private class DeletionCallback implements IAuthorizedCallbackCompleted<Void> {

        private String mServerSideUuid;
        private ModelMapping mModelMapping;

        public DeletionCallback(ModelMapping _modelMapping, String _serverSideUuid) {
            mModelMapping = _modelMapping;
            mServerSideUuid = _serverSideUuid;
        }

        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(Void _next) {
            mMappingController.delete(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class InsertCallback implements IAuthorizedInsertCallbackCompleted<Void> {

        private String mServerSideUuid;
        private ModelMapping mModelMapping;

        public InsertCallback(ModelMapping _modelMapping, String _serverSideUuid) {
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
            mMappingController.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class UpdateCallback implements IAuthorizedCallbackCompleted<Void> {

        private String mServerSideUuid;
        private ModelMapping mModelMapping;

        public UpdateCallback(ModelMapping _modelMapping, String _serverSideUuid) {
            mModelMapping = _modelMapping;
            mServerSideUuid = _serverSideUuid;
        }


        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(Void _next) {
            mMappingController.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class ChangedElementsCallback implements IAuthorizedCallbackCompleted<List<UnitInfo>> {

        private int mGroupId;
        private Date mLastUpdateDate;

        public ChangedElementsCallback(int _groupId, Date _lastUpdateDate) {
            mGroupId = _groupId;
            mLastUpdateDate = _lastUpdateDate;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new UnitSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onCompleted(List<UnitInfo> _next) {
            for (UnitInfo elementInfo : _next) {
                List<ModelMapping> modelMappingList = mMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), elementInfo.getUUID()});

                if (modelMappingList.size() == 0) {
                    // new entry
                    Unit newElement = mUnitController.createUnit(elementInfo.getName());
                    if (newElement == null) {
                        // TODO some error happened
                        continue;
                    }
                    ModelMapping modelMapping = new ModelMapping(null, mGroupId,
                            elementInfo.getUUID(), newElement.mUUID, elementInfo.getLastChanged(),
                            elementInfo.getLastChanged(), false);
                    mMappingController.insert(modelMapping);
                } else {
                    // entry exists local
                    ModelMapping modelMapping = modelMappingList.get(0);
                    Unit element = mUnitController.findById(modelMapping.getClientSideUUID());

                    if (elementInfo.getDeleted()) {
                        // was deleted on server side
                        mUnitController.deleteUnit(element, IUnitController.MODE_UNLINK_REFERENCES);
                        mMappingController.delete(modelMapping);
                        continue;
                    }

                    // else there was an update!
                    if (modelMapping.getLastClientChange().after(elementInfo.getLastChanged())) {
                        // use server side or client side, let the user decide
                        mErrorLogController.insert(elementInfo.getUUID(), ModelType.UNIT,
                                ITask.ReturnCodes.MERGE_CONFLICT, mGroupId);
                        continue;
                    }

                    Unit renamedElement = mUnitController.renameUnit(element, elementInfo.getName());

                    modelMapping.setLastServerChanged(elementInfo.getLastChanged());

                    if (!renamedElement.equals(element)) {
                        // todo give it another name?
                        continue;
                    }
                    mMappingController.update(modelMapping);
                }
            }

            EventBus.getDefault().post(new UnitSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new UnitSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }
    }

    private class ConflictResolveCallback implements IAuthorizedCallbackCompleted<UnitInfo> {
        private int mResolveAction;
        private int mCaseId;
        private int mGroupId;

        public ConflictResolveCallback(int _resolveAction, int _caseId, int _groupId) {
            mResolveAction = _resolveAction;
            mCaseId = _caseId;
            mGroupId = _groupId;
        }

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(UnitInfo _next) {
            if (mResolveAction == ITask.ResolveCodes.RESOLVE_USE_CLIENT_SIDE) {
                // use client side
                // no further action needed?
            } else {
                // use server side
                List<ModelMapping> modelMappingList = mMappingController.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), _next.getUUID()});
                if (modelMappingList.size() == 0) {
                    return;
                }

                ModelMapping modelMapping = modelMappingList.get(0);
                Unit element = mUnitController.findById(modelMapping.getClientSideUUID());
                Unit changedElement = mUnitController.renameUnit(element, _next.getName());

                modelMapping.setLastServerChanged(_next.getLastChanged());
                if (!changedElement.equals(element)) {
                    // todo give it another name?
                    return;
                }
                mMappingController.update(modelMapping);
            }
            mErrorLogController.remove(mCaseId);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }
}
