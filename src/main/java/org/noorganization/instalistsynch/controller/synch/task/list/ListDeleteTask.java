package org.noorganization.instalistsynch.controller.synch.task.list;

import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.model.network.ModelMapping;

/**
 * A task to delete a {@link org.noorganization.instalist.model.ShoppingList}.
 * Created by Desnoo on 14.02.2016.
 */
public class ListDeleteTask implements ITask {

    private ModelMapping mModelMapping;
    private IListController mController;
    private IModelMappingDbController mModelMappingDbController;

    public ListDeleteTask(ModelMapping _modelMapping, IListController _listController, IModelMappingDbController _mappingController) {
        mModelMapping = _modelMapping;
        mController = _listController;
    }

    @Override
    public int execute(int _resolveCode) {
        // if the list was deleted
        ShoppingList list = mController.getListById(mModelMapping.getClientSideUUID());
        // delete the local list
        if (!mController.removeList(list))
            return ReturnCodes.INTERNAL_DB_ERROR;

        if (!mModelMappingDbController.delete(mModelMapping)) {
            // TODO reinsert the list items :(
            mController.addList(list.mName, list.mCategory);
            return ReturnCodes.INTERNAL_DB_ERROR;
        }
        return ReturnCodes.SUCCESS;
    }

    @Override
    public String getServerUUID() {
        return mModelMapping.mServerSideUUID;
    }
}
