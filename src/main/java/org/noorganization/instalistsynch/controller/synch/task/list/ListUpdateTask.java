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
import org.noorganization.instalistsynch.controller.synch.task.comparator.ISynchComperator;
import org.noorganization.instalistsynch.controller.synch.task.comparator.impl.ListComperator;
import org.noorganization.instalistsynch.model.network.ModelMapping;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;

/**
 * A task to insert a {@link org.noorganization.instalist.model.ShoppingList}
 * Created by Desnoo on 14.02.2016.
 */
public class ListUpdateTask implements ITask {
    private static final String TAG = "ListUpdateTask";

    private ListInfo mListInfo;

    private IListController mListController;
    private ICategoryController mCategoryController;

    private IModelMappingDbController mListModelMappingDbController;
    private IModelMappingDbController mCategoryModelMappingDbController;


    private int mGroupId;
    private ModelMapping mListModelMapping;

    private ISynchComperator<ShoppingList, ListInfo> mComperator;

    public ListUpdateTask(ModelMapping _modelMapping, ListInfo listInfo, int groupId) {
        mListModelMapping = _modelMapping;
        mListInfo = listInfo;
        mGroupId = groupId;
        mComperator = new ListComperator();

        mListController = (IListController) GlobalObjects.sControllerMapping.get(eControllerType.LIST);
        mCategoryController = (ICategoryController) GlobalObjects.sControllerMapping.get(eControllerType.CATEGORY);
        mListModelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController();
        mCategoryModelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
    }

    @Override
    public int execute(int _resolveConflict) {
        Date lastServerChange;
        try {
            lastServerChange = ISO8601Utils.parse(mListInfo.getLastChanged(), new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
            return ReturnCodes.PARSE_CONFLICT;
        }

        // there is a local shoppinglist
        // get the local uuid
        String uuid = mListModelMapping.getClientSideUUID();
        ShoppingList list = mListController.getListById(uuid);

        // changes on server are ahead of the last local server update
        if (lastServerChange.after(mListModelMapping.getLastServerChanged())) {

            if (mListModelMapping.getLastClientChange().after(mListModelMapping.getLastServerChanged())) {
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
        Category oldCategory = list.mCategory;
        Category clientCategory = null;

        if (mListInfo.getCategoryUUID() != null) {
            List<ModelMapping> categoryModelMappingList = mCategoryModelMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?", new String[]{mListInfo.getCategoryUUID()});
            if (categoryModelMappingList.size() == 0) {
                return ReturnCodes.WAITING_FOR_RESOURCE;
            }
            clientCategory = mCategoryController.getCategoryByID(categoryModelMappingList.get(0).getClientSideUUID());
        }

        if (list.mCategory == null && mListInfo.getCategoryUUID() != null || list.mCategory != null && mListInfo.getCategoryUUID() == null) {
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
    public String getServerUUID() {
        return mListInfo.getUUID();
    }
}
