package org.noorganization.instalistsynch.controller.synch.impl;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalist.utils.ProviderUtils;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.model.IListNetworkController;
import org.noorganization.instalistsynch.controller.network.model.impl.ModelSynchControllerFactory;
import org.noorganization.instalistsynch.controller.synch.ILocalListSynch;
import org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.network.ModelMapping;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.security.Provider;
import java.text.ParseException;
import java.text.ParsePosition;
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
    public void synchGroup(int _groupId) {
        GroupAuthAccess access = LocalSqliteDbControllerFactory.getAuthAccessDbController(GlobalObjects.getInstance().getApplicationContext()).getGroupAuthAccess(_groupId);
        String authToken = InMemorySessionController.getInstance().getToken(_groupId);
        if (authToken == null) {
            Log.i(TAG, "synchGroup: Auth token is not set.");
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, -1));
            return;
        }
        // get the last update date.
        Date lastUpdateDate = access.getLastUpdated();
        IListNetworkController networkController = ModelSynchControllerFactory.getShoppingListSynchController();
        networkController.getLists(new GetShoppingListResponse(), _groupId, ISO8601Utils.format(lastUpdateDate, true), authToken);
    }

    @Override
    public void resolveConflict(int _conflictId, int _resolveAction) {

    }

    private class GetShoppingListResponse implements IAuthorizedCallbackCompleted<List<ListInfo>> {

        private int mGroupId;

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(List<ListInfo> _next) {
            IListController controller = ControllerFactory.getListController(GlobalObjects.getInstance().getApplicationContext());
            ICategoryController categoryController = ControllerFactory.getCategoryController(GlobalObjects.getInstance().getApplicationContext());
            Cursor cursor = mResolver.query(
                    Uri.withAppendedPath(ProviderUtils.BASE_CONTENT_URI, "list"),
                    ShoppingList.COLUMN.ALL_COLUMNS, null, null, null);

            IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController();
            IModelMappingDbController modelCategoryMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();

            for (ListInfo listInfo : _next) {
                List<ModelMapping> listModelMappingList = modelMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?", new String[]{listInfo.getUUID()});
                if (listModelMappingList.size() == 0) {
                    Category category = null;
                    if (listInfo.getCategoryUUID() != null) {
                        // if this list is assigned to a category
                        List<ModelMapping> categoryModelMappingList = modelCategoryMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?", new String[]{listInfo.getCategoryUUID()});
                        if (categoryModelMappingList.size() == 0) {
                            // TODO create this category
                            // this should never happen
                            // if this happen there is data invalid on the server or on the client.
                        }
                        category = categoryController.getCategoryByID(categoryModelMappingList.get(0).getClientSideUUID());
                    }
                    // insert a list with deps to a category or not
                    ShoppingList list = controller.addList(listInfo.getName(), category);
                    try {
                        Date lastChange = ISO8601Utils.parse(listInfo.getLastChanged(), new ParsePosition(0));
                        // insert this into the database
                        modelMappingDbController.insert(new ModelMapping(null, mGroupId, listInfo.getUUID(), list.mUUID, lastChange, lastChange));
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onCompleted: problem while parsing date", e);
                        // TODO insert an action that has to be done! Rollback!
                        if (!controller.removeList(list)) {
                            // TODO action to schedule this list for removal
                            // skip to the next sent listInfo
                            continue;
                        }
                    }
                } else {
                    // there is a local shoppinglist
                    // get the local uuid
                    ModelMapping modelMapping = listModelMappingList.get(0);
                    String uuid = modelMapping.getClientSideUUID();
                    ShoppingList list = controller.getListById(uuid);
                    if (listInfo.getDeleted()) {
                        // deleted on server side.
                        removeLocalList(controller, modelMappingDbController, modelMapping, list);
                    } else {
                        // not deleted
                        if (!list.mName.contentEquals(listInfo.getName())) {
                            list = controller.renameList(list, listInfo.getName());
                            if (list == null) {
                                // TODO retry the insertion
                                // jump to the next.
                                continue;
                            }
                        }
                        if (list.mCategory == null) {
                            if (listInfo.getCategoryUUID() != null) {
                                // TODO update these entries
                            }
                        }

                    }
                }
            }
        }

        private void removeLocalList(IListController controller, IModelMappingDbController modelMappingDbController, ModelMapping modelMapping, ShoppingList list) {
            // if the list was deleted

            // delete the local list
            if (controller.removeList(list)) {
                if (!modelMappingDbController.delete(modelMapping)) {
                    // TODO what  to do in here.
                }
            } else {
                // removing was not possible.
                // TODO
            }
            // end of delete area.
        }

        @Override
        public void onError(Throwable _e) {

        }
    }
}