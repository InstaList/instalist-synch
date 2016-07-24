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

import org.noorganization.instalist.enums.eControllerType;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.controller.synch.task.eModelType;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.utils.GlobalObjects;

/**
 * A task to delete a {@link org.noorganization.instalist.model.ShoppingList}.
 * Created by Desnoo on 14.02.2016.
 */
public class ListDeleteTask implements ITask {

    private ModelMapping mListModelMapping;
    private IListController mController;
    private IModelMappingDbController mListModelMappingDbController;

    public ListDeleteTask(ModelMapping _modelMapping) {
        mListModelMapping = _modelMapping;
        mController = (IListController) GlobalObjects.sControllerMapping.get(eControllerType.LIST);
        mListModelMappingDbController =  ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
    }

    @Override
    public int executeSynch(int _resolveCode) {
        // if the list was deleted
        ShoppingList list = mController.getListById(mListModelMapping.getClientSideUUID());
        // delete the local list
        if (!mController.removeList(list))
            return ReturnCodes.INTERNAL_DB_ERROR;

        if (!mListModelMappingDbController.delete(mListModelMapping)) {
            // TODO reinsert the list items :(
            mController.addList(list.mName, list.mCategory);
            return ReturnCodes.INTERNAL_DB_ERROR;
        }
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
        return mListModelMapping.mServerSideUUID;
    }
}
