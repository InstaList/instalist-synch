package org.noorganization.instalistsynch.controller.synch.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.ITaskErrorLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.model.IListNetworkController;
import org.noorganization.instalistsynch.controller.network.model.impl.ModelSynchControllerFactory;
import org.noorganization.instalistsynch.controller.synch.ILocalListSynch;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.controller.synch.task.list.ListDeleteTask;
import org.noorganization.instalistsynch.controller.synch.task.list.ListUpdateTask;
import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.events.MergeConflictMessageEvent;
import org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.TaskErrorLog;
import org.noorganization.instalistsynch.model.network.ModelMapping;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Synchronization of the list.
 * Created by Desnoo on 14.02.2016.
 */
public class LocalListSynch implements ILocalListSynch {
    private static final String TAG = "LocalListSynch";
    private ContentResolver mResolver;

    public LocalListSynch() {
        mResolver = GlobalObjects.getInstance().getApplicationContext().getContentResolver();
    }

    @Override
    public void initSynch() {

    }

    @Override
    public void synchGroupFromNetwork(int _groupId) {
        GroupAuthAccess access = LocalSqliteDbControllerFactory.getAuthAccessDbController(GlobalObjects.getInstance().getApplicationContext()).getGroupAuthAccess(_groupId);
        String authToken = InMemorySessionController.getInstance().getToken(_groupId);
        if (authToken == null) {
            Log.i(TAG, "synchGroupFromNetwork: Auth token is not set.");
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, -1));
            return;
        }
        // get the last update date.
        Date lastUpdateDate = access.getLastUpdateFromServer();
        IListNetworkController networkController = ModelSynchControllerFactory.getShoppingListSynchController();
        networkController.getLists(new GetListsResponse(), _groupId, ISO8601Utils.format(lastUpdateDate, true), authToken);
    }

    public void synchGroupFromLocal(int _groupId) {

    }


    @Override
    public void resolveConflict(int _conflictId, int _resolveAction) {
        Context context = GlobalObjects.getInstance().getApplicationContext();
        ITaskErrorLogDbController taskErrorLogDbController = LocalSqliteDbControllerFactory.getTaskErrorLogDbController(context);
        TaskErrorLog taskErrorLog = taskErrorLogDbController.findById(_conflictId);
        if (taskErrorLog == null) {
            Log.i(TAG, "resolveConflict: No error log found for id " + String.valueOf(_conflictId));
            return;
        }
        IListNetworkController networkController = ModelSynchControllerFactory.getShoppingListSynchController();
        String authToken = InMemorySessionController.getInstance().getToken(taskErrorLog.getGroupId());
        if (authToken == null) {
            Log.i(TAG, "synchGroupFromNetwork: Auth token is not set.");
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(taskErrorLog.getGroupId(), -1));
            return;
        }
        networkController.getShoppingList(new GetListResponse(taskErrorLog, _resolveAction), taskErrorLog.getGroupId(), taskErrorLog.getUUID(), authToken);

    }

    private class GetListsResponse implements IAuthorizedCallbackCompleted<List<ListInfo>> {

        private int mGroupId;

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(List<ListInfo> _next) {

            List<ITask> tasks = new ArrayList<>(_next.size());
            // tasks that are waiting for a category.
            List<ITask> tasksAwaitingCategories = new ArrayList<>((_next.size() + 1) / 10);

            Context context = GlobalObjects.getInstance().getApplicationContext();
            IListController controller = ControllerFactory.getListController(context);
            ICategoryController categoryController = ControllerFactory.getCategoryController(context);
            ITaskErrorLogDbController taskErrorLogDbController = LocalSqliteDbControllerFactory.getTaskErrorLogDbController(context);
            IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController();
            IModelMappingDbController modelCategoryMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();

            for (ListInfo listInfo : _next) {
                List<ModelMapping> listModelMappingList = modelMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?", new String[]{listInfo.getUUID()});
                if (listInfo.getDeleted()) {
                    // elements deleted on server.
                    // only delete if there is a linking between server and client
                    if (listModelMappingList.size() > 0)
                        tasks.add(new ListDeleteTask(listModelMappingList.get(0), controller, modelMappingDbController));
                } else {
                    // elements are new or changed
                    ModelMapping modelMapping = listModelMappingList.size() == 0 ? null : listModelMappingList.get(0);
                    tasks.add(new ListUpdateTask(modelMapping, listInfo, controller,
                            categoryController, modelMappingDbController, modelCategoryMappingDbController, mGroupId));
                }
            }

            for (ITask task : tasks) {
                int returnCode = task.execute(ITask.ResolveCodes.NO_RESOLVE);
                if (returnCode != ITask.ReturnCodes.SUCCESS) {
                    if (returnCode == ITask.ReturnCodes.MERGE_CONFLICT) {
                        EventBus.getDefault().post(new MergeConflictMessageEvent(mGroupId, task.getServerUUID()));
                    }
                    taskErrorLogDbController.insert(task.getServerUUID(), eModelType.LIST.ordinal(), returnCode, mGroupId);
                }
            }
            IGroupAuthAccessDbController accessController = LocalSqliteDbControllerFactory.getAuthAccessDbController(GlobalObjects.getInstance().getApplicationContext());
            GroupAuthAccess access = accessController.getGroupAuthAccess(mGroupId);
            access.setLastUpdateFromServer(new Date());
            accessController.update(access);

        }

        @Override
        public void onError(Throwable _e) {
            IGroupAuthAccessDbController accessController = LocalSqliteDbControllerFactory.getAuthAccessDbController(GlobalObjects.getInstance().getApplicationContext());
            GroupAuthAccess access = accessController.getGroupAuthAccess(mGroupId);
            access.setInterrupted(true);
            accessController.update(access);
        }
    }

    private class GetListResponse implements IAuthorizedCallbackCompleted<ListInfo> {

        private TaskErrorLog mTaskErrorLog;
        private int mResolveType;

        public GetListResponse(TaskErrorLog _taskErrorLog, int _resolveType) {
            mTaskErrorLog = _taskErrorLog;
            mResolveType = _resolveType;
        }

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(ListInfo _next) {

            ITask task = null;

            Context context = GlobalObjects.getInstance().getApplicationContext();
            IListController controller = ControllerFactory.getListController(context);
            ICategoryController categoryController = ControllerFactory.getCategoryController(context);
            ITaskErrorLogDbController taskErrorLogDbController = LocalSqliteDbControllerFactory.getTaskErrorLogDbController(context);
            IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController();
            IModelMappingDbController modelCategoryMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();

            List<ModelMapping> listModelMappingList = modelMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?", new String[]{_next.getUUID()});
            // elements are new or changed
            ModelMapping modelMapping = listModelMappingList.size() == 0 ? null : listModelMappingList.get(0);
            task = new ListUpdateTask(modelMapping, _next, controller,
                    categoryController, modelMappingDbController, modelCategoryMappingDbController, mTaskErrorLog.getGroupId());

            if (task == null)
                return;

            int returnCode = task.execute(mResolveType);
            if (returnCode == ITask.ReturnCodes.SUCCESS) {
                taskErrorLogDbController.remove(mTaskErrorLog.getId());
            } else {
                //  taskErrorLogDbController.insert(task.getServerUUID(), ISynchLogDbController.eModelType.LIST.ordinal(), returnCode, mTaskErrorLog.getGroupId());
            }


        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new ErrorMessageEvent(R.string.abc_error_resolving_conflict));
        }
    }
}