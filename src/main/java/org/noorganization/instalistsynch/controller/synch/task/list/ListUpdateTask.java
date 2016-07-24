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

package org.noorganization.instalistsynch.controller.synch.task.list;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.enums.eControllerType;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.controller.synch.comparator.ISynchComperator;
import org.noorganization.instalistsynch.controller.synch.comparator.impl.ListComperator;
import org.noorganization.instalistsynch.controller.synch.task.eModelType;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.util.Date;
import java.util.List;

/**
 * A task to insert a {@link org.noorganization.instalist.model.ShoppingList}
 * Created by Desnoo on 14.02.2016.
 */
public class ListUpdateTask implements ITask {
    private static final String TAG = "ListUpdateTask";

    private ListInfo mListInfo;

    private IListController     mListController;
    private ICategoryController mCategoryController;

    private IModelMappingDbController mListModelMappingDbController;
    private IModelMappingDbController mCategoryModelMappingDbController;


    private int          mGroupId;
    private ModelMapping mListModelMapping;

    private ISynchComperator<ShoppingList, ListInfo> mComperator;

    public ListUpdateTask(ModelMapping _modelMapping, ListInfo listInfo, int groupId) {
        mListModelMapping = _modelMapping;
        mListInfo = listInfo;
        mGroupId = groupId;
        mComperator = new ListComperator();

        mListController =
                (IListController) GlobalObjects.sControllerMapping.get(eControllerType.LIST);
        mCategoryController = (ICategoryController) GlobalObjects.sControllerMapping
                .get(eControllerType.CATEGORY);
        mListModelMappingDbController =
                ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController();
        mCategoryModelMappingDbController =
                ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
    }

    @Override
    public int executeSynch(int _resolveConflict) {
        Date lastServerChange;

        lastServerChange = mListInfo.getLastChanged();

        // there is a local shoppinglist
        // get the local uuid
        String       uuid = mListModelMapping.getClientSideUUID();
        ShoppingList list = mListController.getListById(uuid);

        // changes on server are ahead of the last local server update
        if (lastServerChange.after(mListModelMapping.getLastServerChanged())) {

            if (mListModelMapping.getLastClientChange()
                    .after(mListModelMapping.getLastServerChanged())) {
                mListModelMapping.setLastServerChanged(lastServerChange);
                // server has changed data since last submission and client also --> conflict!
                switch (_resolveConflict) {
                    case ResolveCodes.NO_RESOLVE:
                        return ReturnCodes.MERGE_CONFLICT;
                    case ResolveCodes.RESOLVE_USE_CLIENT_SIDE:
                        // maybe sent update to server.
                        mListModelMappingDbController.update(mListModelMapping);
                        return ReturnCodes.SUCCESS;
                    case ResolveCodes.RESOLVE_USE_SERVER_SIDE:
                        return changeShoppingList(list, lastServerChange);
                    default:
                        return ReturnCodes.INTEGRITY_ERROR;
                }
            }

            mListModelMapping.setLastServerChanged(lastServerChange);

            // not deleted
            if (!mComperator.compare(list, mListInfo)) {
                return changeShoppingList(list, lastServerChange);
            } else {
                return ReturnCodes.SUCCESS;
            }
        }
        return ReturnCodes.SUCCESS;
    }

    private int changeShoppingList(ShoppingList list, Date _lastServerChange) {
        Category oldCategory    = list.mCategory;
        Category clientCategory = null;

        if (mListInfo.getCategoryUUID() != null) {
            List<ModelMapping> categoryModelMappingList = mCategoryModelMappingDbController.get(
                    ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                    new String[]{mListInfo.getCategoryUUID()});
            if (categoryModelMappingList.size() == 0) {
                return ReturnCodes.WAITING_FOR_RESOURCE;
            }
            clientCategory = mCategoryController
                    .getCategoryByID(categoryModelMappingList.get(0).getClientSideUUID());
        }

        if (list.mCategory == null && mListInfo.getCategoryUUID() != null
                || list.mCategory != null && mListInfo.getCategoryUUID() == null) {
            list = mListController.moveToCategory(list, clientCategory);
        }

        if (list.mCategory != null && mListInfo.getCategoryUUID() != null) {
            if (!list.mCategory.equals(clientCategory)) {
                // list was moved from category to new category.
                list = mListController.moveToCategory(list, clientCategory);
            }
        }

        if (list == null) {
            return ReturnCodes.INTERNAL_DB_ERROR;
        }

        ShoppingList oldList = list;
        list = mListController.renameList(list, mListInfo.getName());

        if (list == null) {
            // back to the old status
            mListController.moveToCategory(oldList, oldCategory);
            return ReturnCodes.INTEGRITY_ERROR;
        }

        mListModelMapping.setLastClientChange(new Date());
        mListModelMapping.setLastServerChanged(_lastServerChange);
        mListModelMappingDbController.update(mListModelMapping);
        return ReturnCodes.SUCCESS;
    }

    @Override
    public void executeAsynch(int _resolveCode, ICallbackCompleted _callback) {

    }

    @Override
    public eModelType getTaskModelType() {
        return null;
    }

    @Override
    public String getServerUUID() {
        return mListInfo.getUUID();
    }
}
