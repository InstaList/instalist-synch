package org.noorganization.instalistsynch.controller.synch.task.list;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.model.network.ModelMapping;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;

/**
 * A task to insert a {@link org.noorganization.instalist.model.ShoppingList}
 * Created by Desnoo on 14.02.2016.
 */
public class ListInsertTask implements ITask {
    private static final String TAG = "ListInsertTask";

    private ListInfo mListInfo;

    private IListController mListController;
    private ICategoryController mCategoryController;

    private IModelMappingDbController mModelMappingDbController;
    private IModelMappingDbController mModelCategoryMappingDbController;


    private int mGroupId;
    private ModelMapping mListModelMapping;

    public ListInsertTask(ModelMapping _modelMapping, ListInfo listInfo, IListController listController, ICategoryController categoryController, IModelMappingDbController modelMappingDbController, IModelMappingDbController modelCategoryMappingDbController, int groupId) {
        mListModelMapping = _modelMapping;
        mListInfo = listInfo;
        mListController = listController;
        mCategoryController = categoryController;
        mModelMappingDbController = modelMappingDbController;
        mModelCategoryMappingDbController = modelCategoryMappingDbController;
        mGroupId = groupId;
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

        if (mListModelMapping == null) {
            Category category = null;
            if (mListInfo.getCategoryUUID() != null) {
                // if this list is assigned to a category
                List<ModelMapping> categoryModelMappingList = mModelCategoryMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?", new String[]{mListInfo.getCategoryUUID()});
                if (categoryModelMappingList.size() == 0) {
                    // this should never happen
                    // if this happen there is data invalid on the server or on the client.
                    return ReturnCodes.WAITING_FOR_RESOURCE;
                }
                category = mCategoryController.getCategoryByID(categoryModelMappingList.get(0).getClientSideUUID());
            }
            // insert a list with deps to a category or not
            ShoppingList list = mListController.addList(mListInfo.getName(), category);
            // insert this into the database
            mModelMappingDbController.insert(new ModelMapping(null, mGroupId, mListInfo.getUUID(), list.mUUID, lastServerChange, lastServerChange));
            return ReturnCodes.SUCCESS;
        }

        // model Mapping exists

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
                        mModelMappingDbController.update(mListModelMapping);
                        return ReturnCodes.SUCCESS;
                    case ResolveCodes.RESOLVE_USE_SERVER_SIDE:
                        return changeShoppingList(list);
                    default:
                        return ReturnCodes.INTEGRITY_ERROR;
                }

            }
            mListModelMapping.setLastServerChanged(lastServerChange);

            // not deleted
            if (!list.mName.contentEquals(mListInfo.getName())) {
                // names not equal
                list = mListController.renameList(list, mListInfo.getName());
                if (list == null) {
                    //insertion went wrong
                    return ReturnCodes.INTERNAL_DB_ERROR;
                }
            }
            if (list.mCategory == null) {
                if (mListInfo.getCategoryUUID() != null) {
                    List<ModelMapping> categoryModelMappingList = mModelMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?", new String[]{mListInfo.getCategoryUUID()});
                    if (categoryModelMappingList.size() == 0) {
                        return ReturnCodes.WAITING_FOR_RESOURCE;
                    }
                    Category category = mCategoryController.getCategoryByID(categoryModelMappingList.get(0).getClientSideUUID());
                    if (mListController.moveToCategory(list, category) == null) {
                        return ReturnCodes.INTERNAL_DB_ERROR;
                    }
                }
                mModelMappingDbController.update(mListModelMapping);
                return ReturnCodes.SUCCESS;
            } else {
                return changeShoppingList(list);
            }
        }
        return ReturnCodes.INTEGRITY_ERROR;
    }

    private int changeShoppingList(ShoppingList list) {
        Category category = null;
        if (mListInfo.getCategoryUUID() != null) {
            List<ModelMapping> categoryModelMappingList = mModelMappingDbController.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?", new String[]{mListInfo.getCategoryUUID()});
            if (categoryModelMappingList.size() == 0) {
                return ReturnCodes.WAITING_FOR_RESOURCE;
            }
            category = mCategoryController.getCategoryByID(categoryModelMappingList.get(0).getClientSideUUID());
        }

        if (mListController.moveToCategory(list, category) == null) {
            return ReturnCodes.INTERNAL_DB_ERROR;
        }
        mListModelMapping.setLastClientChange(new Date(System.currentTimeMillis()));
        mModelMappingDbController.update(mListModelMapping);
        return ReturnCodes.SUCCESS;
    }
}
