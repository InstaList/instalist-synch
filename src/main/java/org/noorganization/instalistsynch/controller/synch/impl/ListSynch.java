package org.noorganization.instalistsynch.controller.synch.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eControllerType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;
import org.noorganization.instalistsynch.controller.local.dba.IClientLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.ITaskErrorLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.model.INetworkController;
import org.noorganization.instalistsynch.controller.network.model.RemoteModelAccessControllerFactory;
import org.noorganization.instalistsynch.controller.synch.ISynch;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.controller.synch.task.list.ListDeleteTask;
import org.noorganization.instalistsynch.controller.synch.task.list.ListInsertTask;
import org.noorganization.instalistsynch.controller.synch.task.list.ListUpdateTask;
import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.events.MergeConflictMessageEvent;
import org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.model.TaskErrorLog;
import org.noorganization.instalistsynch.utils.Constants;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Synchronization of the list.
 * Created by Desnoo on 14.02.2016.
 */
public class ListSynch /*implements ISynch*/ {
    /*private static final String TAG = "ListSynch";
    private ContentResolver mResolver;

    private ITaskErrorLogDbController    mTaskErrorLogDbController;
    private IModelMappingDbController    mModelMappingDbController;
    private ISessionController           mSessioncontroller;
    private IListController              mListController;
    private INetworkController<ListInfo> mListNetworkController;

    public ListSynch() {
        Context context = GlobalObjects.getInstance().getApplicationContext();
        mResolver = context.getContentResolver();
        mTaskErrorLogDbController =
                (ITaskErrorLogDbController) GlobalObjects.sControllerMapping
                        .get(eControllerType.ERROR_LOG);
        mModelMappingDbController =
                ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController();
        mSessioncontroller = InMemorySessionController.getInstance();
        mListController = ControllerFactory.getListController(context);
        mListNetworkController =
                RemoteModelAccessControllerFactory.getInstance().getListNetworkController();
    }


    @Override
    public void indexLocalEntries(int _groupId) {
        ModelMapping listMapping;
        Date         currentClientDate = new Date();

        List<ShoppingList> shoppingLists = mListController.getAllLists();
        // assign each list to a model mapping
        for (ShoppingList shoppingList : shoppingLists) {
            listMapping = new ModelMapping(null,
                    _groupId,
                    null,
                    shoppingList.mUUID,
                    new Date(Constants.INITIAL_DATE),
                    currentClientDate, false);
            mModelMappingDbController.insert(listMapping);
        }
    }

    @Override
    public void refreshLocalMapping(int _groupId, Date _sinceTime) {
        // this updates the whole last updateClients fields that were changed since the last synch process.
        // fetch all log data that is related to the group and get the actions since a given date.
        Cursor cursor =
                LocalSqliteDbControllerFactory.getClientLogController(GlobalObjects.getInstance()
                        .getApplicationContext())
                        .getLogsSince(ISO8601Utils.format(_sinceTime), eModelType.LIST);

        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        cursor.moveToFirst();
        try {
            do {
                String clientUuid =
                        cursor.getString(cursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID));
                Date clientChangeDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(
                        LogInfo.COLUMN.ACTION_DATE)).concat("+0000"), new ParsePosition(0));
                eActionType actionType =
                        eActionType.getTypeById(cursor.getInt(cursor
                                .getColumnIndex(LogInfo.COLUMN.ACTION)));

                // should never be the case
                if (actionType == null) {
                    continue;
                }

                // check if there is a model mapping for the item that was in the log.
                List<ModelMapping> modelMappingList = mModelMappingDbController.get(
                        ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?",
                        new String[]{clientUuid});
                ModelMapping modelMapping =
                        modelMappingList.size() == 0 ? null : modelMappingList.get(0);

                // decide which action to do
                switch (actionType) {
                    case INSERT: {
                        // check if the model was not inserted before
                        if (modelMapping == null) {
                            mModelMappingDbController.insert(new ModelMapping(null,
                                    _groupId,
                                    null,
                                    clientUuid,
                                    new Date(Constants.INITIAL_DATE),
                                    clientChangeDate, false));
                        }
                        break;
                    }
                    case UPDATE: {
                        if (modelMapping != null) {
                            modelMapping.setLastClientChange(clientChangeDate);
                            mModelMappingDbController.update(modelMapping);
                        }
                        break;
                    }
                    case DELETE: {
                        if (modelMapping != null) {
                            // remove this model mapping, because it is no longer valid
                            mModelMappingDbController.delete(modelMapping);
                        }
                        break;
                    }
                }
            } while (cursor.moveToNext());
        } catch (ParseException e) {
            Log.e(TAG, "refreshLocalMapping: parse the date went wrong.", e);
            e.printStackTrace();
        } finally {
            cursor.close();
        }
    }

    public void addGroupToList(int _groupId, String _clientMapping) {
        List<ModelMapping> listMapping = mModelMappingDbController.get(
                ModelMapping.COLUMN.GROUP_ID + " = ? AND " + ModelMapping.COLUMN.CLIENT_SIDE_UUID
                        + " LIKE ? ", new String[]{String.valueOf(_groupId), _clientMapping});

        // return if there is already an mapping for this element
        if (listMapping.size() > 0) {
            return;
        }

        mModelMappingDbController.insert(new ModelMapping(null,
                _groupId,
                null,
                _clientMapping,
                new Date(Constants.INITIAL_DATE),
                new Date(), false));
    }

    /**
     * Submits the local results to the server.
     *  @param _groupId    the id of the group, to which the results should be synched.
     * @param _lastUpdate the date when the client last sent the local changes.
     */
    /*@Override
    public void synchLocalToNetwork(int _groupId, Date _lastUpdate) {

        String authToken = mSessioncontroller.getToken(_groupId);
        if (authToken == null) {
            // TODO do something
            return;
        }

        List<ModelMapping> modelMappingList = mModelMappingDbController.get(
                ModelMapping.COLUMN.LAST_CLIENT_CHANGE + " >=  ?  AND "
                        + ModelMapping.COLUMN.GROUP_ID + " = ?",
                new String[]{_lastUpdate, String.valueOf(_groupId)});
        IListController listController =
                ControllerFactory.getListController(GlobalObjects.getInstance()
                        .getApplicationContext());
        IClientLogDbController logDbController =
                LocalSqliteDbControllerFactory.getClientLogController(GlobalObjects.getInstance()
                        .getApplicationContext());


        ListInfo listInfo = new ListInfo();

        for (ModelMapping modelMapping : modelMappingList) {

            List<LogInfo> logInfoList =
                    logDbController.getElementByUuid(modelMapping.getClientSideUUID(),
                            eActionType.DELETE,
                            eModelType.LIST,
                            _lastUpdate);

            // if there was an entry deleted, then also mark this for the server
            listInfo.setDeleted(logInfoList.size() != 0);
            if (listInfo.getDeleted()) {
                // list was deleted
                if (modelMapping.getServerSideUUID() != null) {
                    mListNetworkController.deleteItem(new ListDeleteResponse(_groupId,
                                    modelMapping),
                            _groupId,
                            modelMapping.getServerSideUUID(),
                            authToken);
                }
            } else {
                ShoppingList list = listController.getListById(modelMapping.getClientSideUUID());
                listInfo.setUUID(list.mUUID);
                listInfo.setCategoryUUID(list.mCategory == null ? null : list.mCategory.mUUID);
                listInfo.setLastChanged(modelMapping.getLastClientChange());
                listInfo.setName(list.mName);
                listInfo.setRemoveCategory(false);

                if (modelMapping.getServerSideUUID() == null) {
                    listInfo.setUUID(mModelMappingDbController.generateUuid());
                    modelMapping.setServerSideUUID(listInfo.getUUID());
                    // push this model as insert to the server
                    mListNetworkController.createItem(new ListInsertResponse(_groupId,
                                    modelMapping,
                                    listInfo),
                            _groupId,
                            listInfo,
                            authToken);
                } else {
                    // push the update to the server
                    mListNetworkController.updateItem(new ListUpdateResponse(_groupId,
                                    modelMapping),
                            _groupId,
                            modelMapping.getServerSideUUID(),
                            listInfo,
                            authToken);
                }
            }
        }
    }


    @Override
    public void synchNetworkToLocal(int _groupId, Date _sinceTime) {
        GroupAccess access =
                LocalSqliteDbControllerFactory.getAuthAccessDbController(GlobalObjects.getInstance()
                        .getApplicationContext())
                        .getGroupAuthAccess(_groupId);
        // fetch the auth token
        String authToken = mSessioncontroller.getToken(_groupId);
        if (authToken == null) {
            Log.i(TAG, "synchNetworkToLocal: Auth token is not set.");
            EventBus.getDefault()
                    .post(new UnauthorizedErrorMessageEvent(_groupId, -1));
            return;
        }
        // get the last update date.
        Date lastUpdateDate =
                new Date(System.currentTimeMillis() - 10000000L);//access.getLastUpdateFromServer();

        mListNetworkController.getList(new GetListsResponse(_groupId,
                        ISO8601Utils.format(lastUpdateDate)),
                _groupId,
                ISO8601Utils.format(lastUpdateDate, true),
                authToken);
    }

    @Override
    public void resolveConflict(int _conflictId, int _resolveAction) {
        Context context = GlobalObjects.getInstance()
                .getApplicationContext();
        ITaskErrorLogDbController taskErrorLogDbController =
                LocalSqliteDbControllerFactory.getTaskErrorLogDbController(context);
        TaskErrorLog taskErrorLog =
                taskErrorLogDbController.findById(_conflictId);
        if (taskErrorLog == null) {
            Log.i(TAG, "resolveConflict: No error log found for id " + String.valueOf(_conflictId));
            return;
        }
        String authToken = mSessioncontroller.getToken(taskErrorLog.getGroupId());
        if (authToken == null) {
            Log.i(TAG, "synchNetworkToLocal: Auth token is not set.");
            EventBus.getDefault()
                    .post(new UnauthorizedErrorMessageEvent(taskErrorLog.getGroupId(), -1));
            return;
        }
        mListNetworkController.getItem(new GetListResponse(taskErrorLog, _resolveAction),
                taskErrorLog.getGroupId(),
                taskErrorLog.getUUID(),
                authToken);

    }

    private class GetListsResponse implements IAuthorizedCallbackCompleted<List<ListInfo>> {

        private int    mGroupId;
        private String mLastUpdateDate;

        public GetListsResponse(int groupId, String _lastUpdateDate) {
            mGroupId = groupId;
            mLastUpdateDate = _lastUpdateDate;
        }

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(List<ListInfo> _next) {
            List<ITask> tasks = new ArrayList<>(_next.size());
            // tasks that are waiting for a category.
            // List<ITask> tasksAwaitingCategories = new ArrayList<>((_next.size() + 1) / 10);

            for (ListInfo listInfo : _next) {
                List<ModelMapping> listModelMappingList = mModelMappingDbController.get(
                        ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{listInfo.getUUID()});

                // check if the element on the server was deleted.
                if (listInfo.getDeleted()) {
                    // elements deleted on server.
                    // only delete if there is a linking between server and client
                    if (listModelMappingList.size() > 0) {
                        tasks.add(new ListDeleteTask(listModelMappingList.get(0)));
                    }
                } else {
                    // elements are new or changed
                    ModelMapping modelMapping =
                            listModelMappingList.size() == 0 ? null : listModelMappingList.get(0);
                    if (modelMapping == null) {
                        // element is new.
                        tasks.add(new ListInsertTask(listInfo, mGroupId));
                    } else {
                        // element was updated.
                        tasks.add(new ListUpdateTask(modelMapping, listInfo, mGroupId));
                    }
                }
            }

            for (ITask task : tasks) {
                int returnCode = task.execute(ITask.ResolveCodes.NO_RESOLVE);
                if (returnCode != ITask.ReturnCodes.SUCCESS) {
                    if (returnCode == ITask.ReturnCodes.MERGE_CONFLICT) {
                        EventBus.getDefault()
                                .post(new MergeConflictMessageEvent(mGroupId,
                                        task.getServerUUID()));
                    }
                    mTaskErrorLogDbController.insert(task.getServerUUID(),
                            eModelType.LIST.ordinal(),
                            returnCode,
                            mGroupId);
                }
            }

            IGroupAuthAccessDbController accessController =
                    LocalSqliteDbControllerFactory
                            .getAuthAccessDbController(GlobalObjects.getInstance()
                                    .getApplicationContext());
            GroupAccess access =
                    accessController.getGroupAuthAccess(mGroupId);
            access.setLastUpdateFromServer(new Date());
            accessController.update(access);

        }

        @Override
        public void onError(Throwable _e) {
            IGroupAuthAccessDbController accessController =
                    LocalSqliteDbControllerFactory
                            .getAuthAccessDbController(GlobalObjects.getInstance()
                                    .getApplicationContext());
            GroupAccess access =
                    accessController.getGroupAuthAccess(mGroupId);
            access.setInterrupted(true);
            accessController.update(access);
        }
    }

    private class GetListResponse implements IAuthorizedCallbackCompleted<ListInfo> {

        private TaskErrorLog mTaskErrorLog;
        private int          mResolveType;

        public GetListResponse(TaskErrorLog _taskErrorLog, int _resolveType) {
            mTaskErrorLog = _taskErrorLog;
            mResolveType = _resolveType;
        }

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(ListInfo _next) {

            Context context = GlobalObjects.getInstance()
                    .getApplicationContext();

            ITaskErrorLogDbController taskErrorLogDbController =
                    LocalSqliteDbControllerFactory.getTaskErrorLogDbController(context);
            IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance()
                    .getSqliteShoppingListMappingDbController();

            List<ModelMapping> listModelMappingList =
                    modelMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                            new String[]{_next.getUUID()});
            // elements are new or changed
            ModelMapping modelMapping =
                    listModelMappingList.size() == 0 ? null : listModelMappingList.get(0);
            ITask task =
                    new ListUpdateTask(modelMapping, _next, mTaskErrorLog.getGroupId());

            int returnCode = task.execute(mResolveType);
            if (returnCode == ITask.ReturnCodes.SUCCESS) {
                taskErrorLogDbController.remove(mTaskErrorLog.getId());
            } else {
                //mTaskErrorLogDbController.insert(task.getServerUUID(), ISynchLogDbController.eModelType.LIST.ordinal(), returnCode, mTaskErrorLog.getGroupId());
                EventBus.getDefault()
                        .post(new ErrorMessageEvent(R.string.abc_error_resolving_conflict));
            }
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault()
                    .post(new ErrorMessageEvent(R.string.abc_error_resolving_conflict));
        }
    }

    private class ListDeleteResponse implements IAuthorizedCallbackCompleted<Void> {
        private static final String TAG = "ListDeleteResponse";
        private int          mGroupId;
        private ModelMapping mModelMapping;

        public ListDeleteResponse(int _groupId,
                ModelMapping _modelMapping) {
            mGroupId = _groupId;
            mModelMapping = _modelMapping;
        }

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(Void _next) {
            Log.i(TAG,
                    "onCompleted: delete element from local mapping table: "
                            + mModelMapping.getClientSideUUID());
            mModelMappingDbController.delete(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }

    private class ListUpdateResponse implements IAuthorizedCallbackCompleted<Void> {
        private int          mGroupId;
        private ModelMapping mModelMapping;

        public ListUpdateResponse(int _groupId,
                ModelMapping _modelMapping) {
            mGroupId = _groupId;
            mModelMapping = _modelMapping;
        }

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(Void _next) {
            mModelMappingDbController.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }

    private class ListInsertResponse implements IAuthorizedInsertCallbackCompleted<Void> {
        private int          mGroupId;
        private ModelMapping mModelMapping;
        private ListInfo     mListInfo;

        public ListInsertResponse(int _groupId,
                ModelMapping _modelMapping, ListInfo _listInfo) {
            mGroupId = _groupId;
            mModelMapping = _modelMapping;
            mListInfo = _listInfo;
        }

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onConflict() {
            // generate new id
            mListInfo.setUUID(mModelMappingDbController.generateUuid());
            mModelMapping.setServerSideUUID(mListInfo.getUUID());

            mListNetworkController.createItem(new ListInsertResponse(mModelMapping.getGroupId(),
                            mModelMapping,
                            mListInfo),
                    mModelMapping.getGroupId(),
                    mListInfo,
                    mSessioncontroller.getToken(mModelMapping.getGroupId()));
        }

        @Override
        public void onCompleted(Void _next) {
            mModelMappingDbController.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
            _e.printStackTrace();
            Log.i(TAG, "onError: " + _e.getLocalizedMessage());
        }
    }*/
}