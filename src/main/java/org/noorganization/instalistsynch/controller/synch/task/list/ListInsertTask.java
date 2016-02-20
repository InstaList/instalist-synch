package org.noorganization.instalistsynch.controller.synch.task.list;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.enums.eControllerType;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.model.network.ModelMapping;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.text.ParseException;
import java.text.ParsePosition;
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
    public int execute(int _resolveCode) {

        Date lastServerChange;
        try {
            lastServerChange = ISO8601Utils.parse(mListInfo.getLastChanged(), new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
            return ReturnCodes.PARSE_CONFLICT;
        }

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
        mListModelMappingDbController.insert(new ModelMapping(null, mGroupId, mListInfo.getUUID(), list.mUUID, lastServerChange, lastServerChange));
        return ReturnCodes.SUCCESS;
    }

    @Override
    public String getServerUUID() {
        return mListInfo.getUUID();
    }
}
