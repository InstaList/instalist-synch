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
import org.noorganization.instalistsynch.controller.synch.task.eModelType;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.util.Date;
import java.util.List;

/**
 * An task to update the list.
 * Created by Desnoo on 16.02.2016.
 */
public class ListInsertTask implements ITask {

    private ListInfo mListInfo;

    private IListController mListController;
    private ICategoryController mCategoryController;

    private IModelMappingDbController mListModelMappingDbController;
    private IModelMappingDbController mCategoryModelMappingDbController;


    private int mGroupId;

    public ListInsertTask(ListInfo listInfo, int groupId) {
        mListInfo = listInfo;
        mListController = (IListController) GlobalObjects.sControllerMapping.get(eControllerType.LIST);
        mCategoryController = (ICategoryController) GlobalObjects.sControllerMapping.get(eControllerType.CATEGORY);
        mListModelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController();
        mCategoryModelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        mGroupId = groupId;
    }

    @Override
    public void executeAsynch(int _resolveCode, ICallbackCompleted _callback) {

    }

    @Override
    public eModelType getTaskModelType() {
        return null;
    }

    @Override
    public int executeSynch(int _resolveCode) {

        Date lastServerChange;
        lastServerChange = mListInfo.getLastChanged();


        Category category = null;
        if (mListInfo.getCategoryUUID() != null) {
            // if this list is assigned to a category
            List<ModelMapping> categoryModelMappingList = mCategoryModelMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?", new String[]{mListInfo.getCategoryUUID()});
            if (categoryModelMappingList.size() == 0) {
                // this should never happen
                // if this happen there is data invalid on the server or on the client.
                return ReturnCodes.WAITING_FOR_RESOURCE;
            }
            category = mCategoryController.getCategoryByID(categoryModelMappingList.get(0).getClientSideUUID());
        }
        // insert a list with deps to a category or not
        ShoppingList list = mListController.addList(mListInfo.getName(), category);
        if (list == null) {
            // TODO name could be twice used
            return ReturnCodes.INTEGRITY_ERROR;
        }

        // insert this into the database
        mListModelMappingDbController.insert(new ModelMapping(null, mGroupId, mListInfo.getUUID(), list.mUUID, lastServerChange, lastServerChange, false));
        return ReturnCodes.SUCCESS;
    }

    @Override
    public String getServerUUID() {
        return mListInfo.getUUID();
    }
}
