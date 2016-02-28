package org.noorganization.instalistsynch.controller.synch.impl;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.IngredientInfo;
import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.Ingredient;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Recipe;
import org.noorganization.instalist.presenter.IProductController;
import org.noorganization.instalist.presenter.IRecipeController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;
import org.noorganization.instalistsynch.controller.local.dba.IClientLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.ITaskErrorLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.TaskErrorLogDbController;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.model.INetworkController;
import org.noorganization.instalistsynch.controller.network.model.RemoteModelAccessControllerFactory;
import org.noorganization.instalistsynch.controller.synch.ISynch;
import org.noorganization.instalistsynch.controller.synch.task.ITask;
import org.noorganization.instalistsynch.events.IngredientSynchFromNetworkFinished;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.model.TaskErrorLog;
import org.noorganization.instalistsynch.utils.Constants;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;

/**
 * Synchronization of all lists.
 * Created by Desnoo on 27.02.2016.
 */
public class IngredientSynch implements ISynch {
    private static final String TAG = "IngredientSynch";

    private ISessionController mSessionController;
    private IRecipeController mRecipeController;
    private IProductController mProductController;


    private IModelMappingDbController mIngredientModelMapping;
    private IModelMappingDbController mRecipeModelMapping;
    private IModelMappingDbController mProductModelMapping;

    private IClientLogDbController mClientLogDbController;
    private IGroupAuthDbController mGroupAuthDbController;
    private INetworkController<IngredientInfo> mIngredientInfoNetworkController;
    private ITaskErrorLogDbController mTaskErrorLogDbController;

    private eModelType mModelType;
    private EventBus mEventBus;

    public IngredientSynch(eModelType _type) {
        mModelType = _type;

        Context context = GlobalObjects.getInstance().getApplicationContext();
        mSessionController = InMemorySessionController.getInstance();
        mRecipeController = ControllerFactory.getRecipeController(context);
        mProductController = ControllerFactory.getProductController(context);

        mIngredientModelMapping = ModelMappingDbFactory.getInstance().getSqliteIngredientMappingDbController();
        mRecipeModelMapping = ModelMappingDbFactory.getInstance().getSqliteRecipeMappingController();
        mProductModelMapping = ModelMappingDbFactory.getInstance().getSqliteProductMappingController();

        mClientLogDbController = LocalSqliteDbControllerFactory.getClientLogController(context);
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(context);
        mIngredientInfoNetworkController = RemoteModelAccessControllerFactory.getInstance().getIngredientNetworkController();
        mTaskErrorLogDbController = TaskErrorLogDbController.getInstance(context);
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void indexLocalEntries(int _groupId) {
        List<ModelMapping> modelMappings = mIngredientModelMapping.get(null, null);
        if (modelMappings.size() > 0) {
            return;
        }

        List<Recipe> recipeList = mRecipeController.listAll();
        List<Ingredient> ingredientList = new ArrayList<>();

        for (Recipe recipe : recipeList) {
            ingredientList.addAll(mRecipeController.getIngredients(recipe.mUUID));
        }

        ModelMapping modelMapping;

        for (Ingredient ingredient : ingredientList) {
            modelMapping =
                    new ModelMapping(null, _groupId, null, ingredient.mUUID, new Date(Constants.INITIAL_DATE), new Date(), false);
            mIngredientModelMapping.insert(modelMapping);
        }
    }

    @Override
    public void indexLocal(int _groupId, Date _lastIndexTime) {
        String lastIndexTime = ISO8601Utils.format(_lastIndexTime, false, TimeZone.getTimeZone("GMT+0000"));//.concat("+0000");
        boolean isLocal = false;
        GroupAuth groupAuth = mGroupAuthDbController.getLocalGroup();
        if (groupAuth != null) {
            isLocal = groupAuth.getGroupId() == _groupId;
        }
        Cursor logCursor =
                mClientLogDbController.getLogsSince(lastIndexTime, mModelType);
        if (logCursor.getCount() == 0) {
            logCursor.close();
            return;
        }

        try {
            while (logCursor.moveToNext()) {
                // fetch the action type
                int actionId = logCursor.getInt(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION));
                eActionType actionType = eActionType.getTypeById(actionId);

                List<ModelMapping> modelMappingList = mIngredientModelMapping.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND " +
                                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?", new String[]{
                                String.valueOf(_groupId),
                                logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID))});
                ModelMapping modelMapping =
                        modelMappingList.size() == 0 ? null : modelMappingList.get(0);

                switch (actionType) {
                    case INSERT:
                        // skip insertion because this should be decided by the user if the non local groups should have access to the category
                        // and also skip if a mapping for this case already exists!
                        if (!isLocal || modelMapping != null) {
                            continue;
                        }

                        String clientUuid = logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID));
                        Date clientDate = ISO8601Utils.parse(logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE)), new ParsePosition(0));
                        modelMapping = new ModelMapping(null, groupAuth.getGroupId(), null, clientUuid, new Date(Constants.INITIAL_DATE), clientDate, false);
                        mIngredientModelMapping.insert(modelMapping);
                        break;
                    case UPDATE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        String timeString = logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mIngredientModelMapping.update(modelMapping);
                        break;
                    case DELETE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        modelMapping.setDeleted(true);
                        timeString = logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mIngredientModelMapping.update(modelMapping);
                        break;
                    default:
                }

            }
        } catch (Exception e) {
            logCursor.close();
        }
    }

    @Override
    public void addGroupToMapping(int _groupId, String _clientUuid) {
        Date lastUpdate = mClientLogDbController.getLeastRecentUpdateTimeForUuid(_clientUuid);
        if (lastUpdate == null) {
            return;
        }
        ModelMapping modelMapping = new ModelMapping(null, _groupId, null, _clientUuid, new Date(Constants.INITIAL_DATE), lastUpdate, false);
        mIngredientModelMapping.insert(modelMapping);
    }

    @Override
    public void removeGroupFromMapping(int _groupId, String _clientUuid) {
        List<ModelMapping> modelMappingList = mIngredientModelMapping.get(
                ModelMapping.COLUMN.GROUP_ID
                        + " = ? AND " + ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?",
                new String[]{String.valueOf(_groupId), _clientUuid});
        if (modelMappingList.size() == 0) {
            return;
        }
        mIngredientModelMapping.delete(modelMappingList.get(0));
    }

    @Override
    public void synchLocalToNetwork(int _groupId, Date _lastUpdate) {
        String lastUpdateString = ISO8601Utils.format(_lastUpdate, false, TimeZone.getTimeZone("GMT+0000"));
        String authToken = mSessionController.getToken(_groupId);

        if (authToken == null) {
            // todo do some caching of this action
            return;
        }

        List<ModelMapping> modelMappingList = mIngredientModelMapping.get(
                ModelMapping.COLUMN.LAST_CLIENT_CHANGE + " >= ? ", new String[]{lastUpdateString});
        for (ModelMapping modelMapping : modelMappingList) {
            if (modelMapping.isDeleted()) {
                // delete the item
                mIngredientInfoNetworkController.deleteItem(new DeleteResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, modelMapping.getServerSideUUID(), authToken);
            } else if (modelMapping.getServerSideUUID() == null) {
                // insert new
                Ingredient ingredient = mRecipeController.findIngredientById(modelMapping.getClientSideUUID());
                if (ingredient == null) {
                    continue;
                }

                IngredientInfo ingredientInfo = getIngredientInfo(ingredient, _groupId, modelMapping);
                if (ingredientInfo == null)
                    continue;

                mIngredientInfoNetworkController.createItem(new InsertResponse(modelMapping, ingredientInfo.getUUID()), _groupId, ingredientInfo, authToken);
            } else {
                // update existing
                Ingredient ingredient = mRecipeController.findIngredientById(modelMapping.getClientSideUUID());
                if (ingredient == null) {
                    // probably the item was deleted
                    mIngredientInfoNetworkController.deleteItem(new DeleteResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, modelMapping.getServerSideUUID(), authToken);
                    continue;
                }
                IngredientInfo ingredientInfo = getIngredientInfo(ingredient, _groupId, modelMapping);
                if (ingredientInfo == null)
                    continue;

                mIngredientInfoNetworkController.updateItem(new UpdateResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, ingredientInfo.getUUID(), ingredientInfo, authToken);
            }
        }
    }


    /**
     * Get the ingredient info from the ingredient model.
     *
     * @param _ingredient   the ingredient that infos are extracted.
     * @param _groupId      the id of the group.
     * @param _modelMapping the ingredient model mapping.
     * @return the related IngredientInfo object or null if something failed.
     */
    private IngredientInfo getIngredientInfo(Ingredient _ingredient, int _groupId, ModelMapping _modelMapping) {
        IngredientInfo ingredientInfo = new IngredientInfo();
        String uuid = mIngredientModelMapping.generateUuid();

        ingredientInfo.setUUID(uuid);
        ingredientInfo.setAmount(_ingredient.mAmount);

        String productUuid = _ingredient.mProduct.mUUID;
        String recipeUuid = _ingredient.mRecipe.mUUID;

        // fetch the mapping of product and recipe to know the id on the server side.
        List<ModelMapping> productMappingList = mProductModelMapping.get(ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ? ",
                new String[]{productUuid, String.valueOf(_groupId)});
        List<ModelMapping> recipeMappingList = mRecipeModelMapping.get(ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ? ",
                new String[]{recipeUuid, String.valueOf(_groupId)});

        if (productMappingList.size() == 0 || recipeMappingList.size() == 0) {
            // todo indicate that there is a resource missing
            return null;
        }

        ModelMapping productMapping = productMappingList.get(0);
        ModelMapping recipeMapping = recipeMappingList.get(0);

        ingredientInfo.setProductUUID(productMapping.getServerSideUUID());
        ingredientInfo.setRecipeUUID(recipeMapping.getServerSideUUID());

        ingredientInfo.setLastChanged(_modelMapping.getLastClientChange());
        ingredientInfo.setDeleted(false);
        return ingredientInfo;
    }

    /**
     * Get the requested model mapping.
     *
     * @param _columnName          the affected column for the where clause.
     * @param _firstWhereParameter the parameter for this where clause
     * @param _groupId             the id of the group.
     * @return the model mapping or null if there exists none.
     */
    private ModelMapping getModelMapping(IModelMappingDbController modelMappingDbController, String _columnName, String _firstWhereParameter, int _groupId) {
        List<ModelMapping> mappingList = modelMappingDbController.get(_columnName + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ? ",
                new String[]{_firstWhereParameter, String.valueOf(_groupId)});
        if (mappingList.size() == 0) {
            // todo indicate that there is a resource missing
            return null;
        }

        return mappingList.get(0);
    }

    @Override
    public void synchNetworkToLocal(int _groupId, Date _sinceTime) {
        String authToken = mSessionController.getToken(_groupId);
        if (authToken == null) {
            return;
        }
        mIngredientInfoNetworkController.getList(new GetListResponse(_groupId, _sinceTime), _groupId, ISO8601Utils.format(_sinceTime, false, TimeZone.getTimeZone("GMT+0000")).concat("+0000"), authToken);
    }

    @Override
    public void resolveConflict(int _conflictId, int _resolveAction) {
        TaskErrorLog log = mTaskErrorLogDbController.findById(_conflictId);
        if (log == null) {
            return;
        }
        String authToken = mSessionController.getToken(log.getGroupId());
        if (authToken == null) {
            return;
        }

        mIngredientInfoNetworkController.getItem(new GetItemConflictResolveResponse(_resolveAction, _conflictId, log.getGroupId()), log.getGroupId(), log.getUUID(), authToken);
    }

    private class DeleteResponse implements IAuthorizedCallbackCompleted<Void> {

        private String mServerSideUuid;
        private ModelMapping mModelMapping;

        public DeleteResponse(ModelMapping _modelMapping, String _serverSideUuid) {
            mModelMapping = _modelMapping;
            mServerSideUuid = _serverSideUuid;
        }

        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(Void _next) {
            mIngredientModelMapping.delete(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class InsertResponse implements IAuthorizedInsertCallbackCompleted<Void> {

        private String mServerSideUuid;
        private ModelMapping mModelMapping;

        public InsertResponse(ModelMapping _modelMapping, String _serverSideUuid) {
            mModelMapping = _modelMapping;
            mServerSideUuid = _serverSideUuid;
        }

        @Override
        public void onConflict() {
            // todo
        }


        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(Void _next) {
            mModelMapping.setLastServerChanged(new Date());
            mModelMapping.setServerSideUUID(mServerSideUuid);
            mIngredientModelMapping.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class UpdateResponse implements IAuthorizedCallbackCompleted<Void> {

        private String mServerSideUuid;
        private ModelMapping mModelMapping;

        public UpdateResponse(ModelMapping _modelMapping, String _serverSideUuid) {
            mModelMapping = _modelMapping;
            mServerSideUuid = _serverSideUuid;
        }


        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(Void _next) {
            mIngredientModelMapping.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class GetListResponse implements IAuthorizedCallbackCompleted<List<IngredientInfo>> {

        private int mGroupId;
        private Date mLastUpdateDate;

        public GetListResponse(int _groupId, Date _lastUpdateDate) {
            mGroupId = _groupId;
            mLastUpdateDate = _lastUpdateDate;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new IngredientSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onCompleted(List<IngredientInfo> _next) {
            for (IngredientInfo ingredientInfo : _next) {
                List<ModelMapping> modelMappingList = mIngredientModelMapping.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), ingredientInfo.getUUID()});

                if (modelMappingList.size() == 0) {
                    ModelMapping recipeMapping = getModelMapping(mRecipeModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, ingredientInfo.getRecipeUUID(), mGroupId);
                    ModelMapping productMapping = getModelMapping(mProductModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, ingredientInfo.getProductUUID(), mGroupId);

                    if (recipeMapping == null || productMapping == null) {
                        // todo not inserted info maybe requery this stuff
                        continue;
                    }

                    Recipe recipe = mRecipeController.findById(recipeMapping.getClientSideUUID());
                    Product product = mProductController.findById(productMapping.getClientSideUUID());
                    // new entry
                    Ingredient newIngredient = mRecipeController.addOrChangeIngredient(recipe, product, ingredientInfo.getAmount());
                    if (newIngredient == null) {
                        // TODO some error happened
                        continue;
                    }
                    ModelMapping modelMapping = new ModelMapping(null, mGroupId, ingredientInfo.getUUID(),
                            newIngredient.mUUID, ingredientInfo.getLastChanged(), ingredientInfo.getLastChanged(), false);
                    mIngredientModelMapping.insert(modelMapping);
                } else {
                    ModelMapping recipeMapping = getModelMapping(mRecipeModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, ingredientInfo.getRecipeUUID(), mGroupId);
                    ModelMapping productMapping = getModelMapping(mProductModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, ingredientInfo.getProductUUID(), mGroupId);

                    if (recipeMapping == null || productMapping == null) {
                        // todo not inserted info maybe requery this stuff
                        continue;
                    }

                    Recipe recipe = mRecipeController.findById(recipeMapping.getClientSideUUID());
                    Product product = mProductController.findById(productMapping.getClientSideUUID());

                    // entry exists local
                    ModelMapping modelMapping = modelMappingList.get(0);
                    Ingredient ingredient = mRecipeController.findIngredientById(modelMapping.getClientSideUUID());

                    if (ingredientInfo.getDeleted()) {
                        // was deleted on server side
                        mRecipeController.removeIngredient(ingredient);
                        mIngredientModelMapping.delete(modelMapping);
                        continue;
                    }

                    // else there was an update!
                    if (modelMapping.getLastClientChange().after(ingredientInfo.getLastChanged())) {
                        // use server side or client side, let the user decide
                        mTaskErrorLogDbController.insert(ingredientInfo.getUUID(), mModelType.ordinal(), ITask.ReturnCodes.MERGE_CONFLICT, mGroupId);
                        continue;
                    }

                    Ingredient updatedIngredient = mRecipeController.addOrChangeIngredient(recipe, product, ingredientInfo.getAmount());
                    if (updatedIngredient == null) {
                        Log.e(TAG, "onCompleted: update of ingredient from server went wrong.");
                    }
                    modelMapping.setLastServerChanged(ingredientInfo.getLastChanged());

                    mIngredientModelMapping.update(modelMapping);
                }
            }

            EventBus.getDefault().post(new IngredientSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new IngredientSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }
    }

    private class GetItemConflictResolveResponse implements IAuthorizedCallbackCompleted<IngredientInfo> {
        private int mResolveAction;
        private int mCaseId;
        private int mGroupId;

        public GetItemConflictResolveResponse(int _resolveAction, int _caseId, int _groupId) {
            mResolveAction = _resolveAction;
            mCaseId = _caseId;
            mGroupId = _groupId;
        }

        @Override
        public void onUnauthorized(int _groupId) {

        }

        @Override
        public void onCompleted(IngredientInfo _next) {
            if (mResolveAction == ITask.ResolveCodes.RESOLVE_USE_CLIENT_SIDE) {
                // use client side
                // no further action needed?
            } else {
                // use server side
                List<ModelMapping> modelMappingList = mIngredientModelMapping.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), _next.getUUID()});
                if (modelMappingList.size() == 0) {
                    return;
                }

                ModelMapping modelMapping = modelMappingList.get(0);

                ModelMapping recipeMapping = getModelMapping(mRecipeModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, _next.getRecipeUUID(), mGroupId);
                ModelMapping productMapping = getModelMapping(mProductModelMapping, ModelMapping.COLUMN.SERVER_SIDE_UUID, _next.getProductUUID(), mGroupId);

                if (recipeMapping == null || productMapping == null) {
                    // todo not inserted info maybe requery this stuff
                    return;
                }

                Recipe recipe = mRecipeController.findById(recipeMapping.getClientSideUUID());
                Product product = mProductController.findById(productMapping.getClientSideUUID());

                // entry exists local
                Ingredient ingredient = mRecipeController.findIngredientById(modelMapping.getClientSideUUID());

                Ingredient updatedIngredient = mRecipeController.addOrChangeIngredient(recipe, product, _next.getAmount());
                if (updatedIngredient == null) {
                    Log.e(TAG, "onCompleted: update of ingredient from server went wrong.");
                }
                modelMapping.setLastServerChanged(_next.getLastChanged());

                mIngredientModelMapping.update(modelMapping);

            }
            mTaskErrorLogDbController.remove(mCaseId);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }
}
