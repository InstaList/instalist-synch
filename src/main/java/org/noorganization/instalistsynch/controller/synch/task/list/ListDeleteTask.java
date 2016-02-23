package org.noorganization.instalistsynch.controller.synch.task.list;

import org.noorganization.instalist.enums.eControllerType;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
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
    public int execute(int _resolveCode) {
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
    public String getServerUUID() {
        return mListModelMapping.mServerSideUUID;
    }
}
