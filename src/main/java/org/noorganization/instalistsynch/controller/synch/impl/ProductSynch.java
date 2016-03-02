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

package org.noorganization.instalistsynch.controller.synch.impl;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.comm.message.ProductInfo;
import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.model.Product;
import org.noorganization.instalist.model.Unit;
import org.noorganization.instalist.presenter.IProductController;
import org.noorganization.instalist.presenter.IUnitController;
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
import org.noorganization.instalistsynch.events.ProductSynchFromNetworkFinished;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.model.TaskErrorLog;
import org.noorganization.instalistsynch.utils.Constants;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.greenrobot.event.EventBus;

/**
 * Synchronization of all lists.
 * Created by Desnoo on 27.02.2016.
 */
public class ProductSynch implements ISynch {
    private static final String TAG = "ProductSynch";

    private ISessionController mSessionController;
    private IProductController mProductController;
    private IModelMappingDbController mProductModelMapping;

    private IClientLogDbController mClientLogDbController;
    private IGroupAuthDbController mGroupAuthDbController;
    private INetworkController<ProductInfo> mProductetworkController;
    private ITaskErrorLogDbController mTaskErrorLogDbController;

    private eModelType mModelType;
    private EventBus mEventBus;

    public ProductSynch(eModelType _type) {
        mModelType = _type;

        Context context = GlobalObjects.getInstance().getApplicationContext();
        mSessionController = InMemorySessionController.getInstance();
        mProductController = ControllerFactory.getProductController(context);
        mProductModelMapping =
                ModelMappingDbFactory.getInstance().getSqliteProductMappingController();

        mClientLogDbController = LocalSqliteDbControllerFactory.getClientLogController(context);
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(context);
        mProductetworkController = RemoteModelAccessControllerFactory.getInstance().getProductNetworkController();
        mTaskErrorLogDbController = TaskErrorLogDbController.getInstance(context);
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void indexLocalEntries(int _groupId) {
        List<ModelMapping> modelMappings = mProductModelMapping.get(null, null);
        if (modelMappings.size() > 0) {
            return;
        }

        List<Product> productList = mProductController.listAll();
        ModelMapping modelMapping;

        for (Product product : productList) {
            modelMapping =
                    new ModelMapping(null, _groupId, null, product.mUUID, new Date(Constants.INITIAL_DATE), new Date(), false);
            mProductModelMapping.insert(modelMapping);
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

                List<ModelMapping> modelMappingList = mProductModelMapping.get(
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
                        mProductModelMapping.insert(modelMapping);
                        break;
                    case UPDATE:
                        if (modelMapping == null) {
                            Log.i(TAG, "indexLocal: the model is null but shouldn't be");
                            continue;
                        }
                        String timeString = logCursor.getString(logCursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
                        clientDate = ISO8601Utils.parse(timeString, new ParsePosition(0));
                        modelMapping.setLastClientChange(clientDate);
                        mProductModelMapping.update(modelMapping);
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
                        mProductModelMapping.update(modelMapping);
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
        mProductModelMapping.insert(modelMapping);
    }

    @Override
    public void removeGroupFromMapping(int _groupId, String _clientUuid) {
        List<ModelMapping> modelMappingList = mProductModelMapping.get(
                ModelMapping.COLUMN.GROUP_ID
                        + " = ? AND " + ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ?",
                new String[]{String.valueOf(_groupId), _clientUuid});
        if (modelMappingList.size() == 0) {
            return;
        }
        mProductModelMapping.delete(modelMappingList.get(0));
    }

    @Override
    public void synchLocalToNetwork(int _groupId, Date _lastUpdate) {
        String lastUpdateString = ISO8601Utils.format(_lastUpdate, false, TimeZone.getTimeZone("GMT+0000"));
        String authToken = mSessionController.getToken(_groupId);

        if (authToken == null) {
            // todo do some caching of this action
            return;
        }

        List<ModelMapping> modelMappingList = mProductModelMapping.get(
                ModelMapping.COLUMN.LAST_CLIENT_CHANGE + " >= ? ", new String[]{lastUpdateString});
        for (ModelMapping modelMapping : modelMappingList) {
            if (modelMapping.isDeleted()) {
                // delete the item
                mProductetworkController.deleteItem(new DeleteResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, modelMapping.getServerSideUUID(), authToken);
            } else if (modelMapping.getServerSideUUID() == null) {
                // insert new
                Product product = mProductController.findById(modelMapping.getClientSideUUID());
                if (product == null) {
                    continue;
                }
                ProductInfo productInfo = getProductInfo(product, _groupId, modelMapping);
                mProductetworkController.createItem(new InsertResponse(modelMapping, productInfo.getUUID()), _groupId, productInfo, authToken);
            } else {
                // update existing
                Product product = mProductController.findById(modelMapping.getClientSideUUID());
                if (product == null) {
                    // probably the item was deleted
                    mProductetworkController.deleteItem(new DeleteResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, modelMapping.getServerSideUUID(), authToken);
                    continue;
                }

                ProductInfo productInfo = getProductInfo(product, _groupId, modelMapping);
                mProductetworkController.updateItem(new UpdateResponse(modelMapping, modelMapping.getServerSideUUID()), _groupId, productInfo.getUUID(), productInfo, authToken);
            }
        }
    }

    /**
     * Fill the productInfo object.
     *
     * @param _product      the product the product info is based on.
     * @param _groupId      the id of the group.
     * @param _modelMapping the model mapping of this model type.
     * @return the productinfo object filled with the given data.
     */
    private ProductInfo getProductInfo(Product _product, int _groupId, ModelMapping _modelMapping) {
        ProductInfo productInfo = new ProductInfo();
        String uuid = mProductModelMapping.generateUuid();
        productInfo.setUUID(uuid);
        productInfo.setName(_product.mName);
        productInfo.setStepAmount(_product.mStepAmount);
        productInfo.setDefaultAmount(_product.mDefaultAmount);
        productInfo.setRemoveUnit(false);

        if (_product.mUnit != null && !_product.mUnit.mName.contentEquals("-")) {
            IModelMappingDbController unitModelMappingController = ModelMappingDbFactory.getInstance().getSqliteUnitMappingController();
            List<ModelMapping> unitModelMappingList = unitModelMappingController.get(ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ?",
                    new String[]{_product.mUnit.mUUID, String.valueOf(_groupId)});
            if (unitModelMappingList.size() == 1) {
                ModelMapping unitMapping = unitModelMappingList.get(0);
                productInfo.setUUID(unitMapping.getServerSideUUID());
            }
        }
        Date lastChanged = new Date(_modelMapping.getLastClientChange().getTime() -Constants.NETWORK_OFFSET);
        productInfo.setLastChanged(lastChanged);
        productInfo.setDeleted(false);

        return productInfo;
    }


    @Override
    public void synchNetworkToLocal(int _groupId, Date _sinceTime) {
        String authToken = mSessionController.getToken(_groupId);
        if (authToken == null) {
            return;
        }
        mProductetworkController.getList(new GetListResponse(_groupId, _sinceTime), _groupId, ISO8601Utils.format(_sinceTime, false, TimeZone.getTimeZone("GMT+0000")).concat("+0000"), authToken);
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

        mProductetworkController.getItem(new GetItemConflictResolveResponse(_resolveAction, _conflictId, log.getGroupId()), log.getGroupId(), log.getUUID(), authToken);
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
            mProductModelMapping.delete(mModelMapping);
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
            mProductModelMapping.update(mModelMapping);
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
            mProductModelMapping.update(mModelMapping);
        }

        @Override
        public void onError(Throwable _e) {
        }
    }

    private class GetListResponse implements IAuthorizedCallbackCompleted<List<ProductInfo>> {

        private int mGroupId;
        private Date mLastUpdateDate;

        public GetListResponse(int _groupId, Date _lastUpdateDate) {
            mGroupId = _groupId;
            mLastUpdateDate = _lastUpdateDate;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new ProductSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onCompleted(List<ProductInfo> _next) {
            for (ProductInfo productInfo : _next) {
                List<ModelMapping> modelMappingList = mProductModelMapping.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), productInfo.getUUID()});

                if (modelMappingList.size() == 0) {
                    IUnitController unitController = ControllerFactory.getUnitController(GlobalObjects.getInstance().getApplicationContext());
                    Unit unit = unitController.getDefaultUnit();
                    if (productInfo.getUnitUUID() != null) {
                        IModelMappingDbController unitMapping = ModelMappingDbFactory.getInstance().getSqliteUnitMappingController();
                        List<ModelMapping> unitMappingList = unitMapping.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ?",
                                new String[]{productInfo.getUnitUUID(), String.valueOf(mGroupId)});
                        if (unitMappingList.size() == 1) {
                            unit = unitController.findById(unitMappingList.get(0).getClientSideUUID());
                        }
                    }
                    // new entry
                    Product newProduct = mProductController.createProduct(productInfo.getName(),
                            unit, productInfo.getDefaultAmount(), productInfo.getStepAmount());

                    if (newProduct == null) {
                        // TODO some error happened
                        continue;
                    }
                    ModelMapping modelMapping = new ModelMapping(null, mGroupId, productInfo.getUUID(),
                            newProduct.mUUID, productInfo.getLastChanged(), productInfo.getLastChanged(), false);
                    mProductModelMapping.insert(modelMapping);
                } else {
                    // entry exists local
                    ModelMapping modelMapping = modelMappingList.get(0);
                    Product product = mProductController.findById(modelMapping.getClientSideUUID());

                    if (productInfo.getDeleted()) {
                        // was deleted on server side
                        mProductController.removeProduct(product, true);
                        mProductModelMapping.delete(modelMapping);
                        continue;
                    }

                    // else there was an update!
                    if (modelMapping.getLastClientChange().after(productInfo.getLastChanged())) {
                        // use server side or client side, let the user decide
                        mTaskErrorLogDbController.insert(productInfo.getUUID(), mModelType.ordinal(), ITask.ReturnCodes.MERGE_CONFLICT, mGroupId);
                        continue;
                    }

                    IUnitController unitController = ControllerFactory.getUnitController(GlobalObjects.getInstance().getApplicationContext());
                    Unit unit = unitController.getDefaultUnit();
                    if (productInfo.getUnitUUID() != null) {
                        IModelMappingDbController unitMapping = ModelMappingDbFactory.getInstance().getSqliteUnitMappingController();
                        List<ModelMapping> unitMappingList = unitMapping.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ?",
                                new String[]{productInfo.getUnitUUID(), String.valueOf(mGroupId)});
                        if (unitMappingList.size() == 1) {
                            unit = unitController.findById(unitMappingList.get(0).getClientSideUUID());
                        }
                    }
                    product.mName = productInfo.getName();
                    product.mUnit = unit;
                    product.mDefaultAmount = productInfo.getDefaultAmount();
                    product.mStepAmount = productInfo.getStepAmount();

                    // new entry
                    Product productWithUpdate = mProductController.modifyProduct(product);
                    if(productWithUpdate == null){
                        Log.e(TAG, "onCompleted: update of product failed");
                        continue;
                    }

                    modelMapping.setLastServerChanged(productInfo.getLastChanged());
                    mProductModelMapping.update(modelMapping);
                }
            }

            EventBus.getDefault().post(new ProductSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new ProductSynchFromNetworkFinished(mLastUpdateDate, mGroupId));
        }
    }

    private class GetItemConflictResolveResponse implements IAuthorizedCallbackCompleted<ProductInfo> {
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
        public void onCompleted(ProductInfo _next) {
            if (mResolveAction == ITask.ResolveCodes.RESOLVE_USE_CLIENT_SIDE) {
                // use client side
                // no further action needed?
            } else {
                // use server side
                List<ModelMapping> modelMappingList = mProductModelMapping.get(
                        ModelMapping.COLUMN.GROUP_ID + " = ? AND "
                                + ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ?",
                        new String[]{String.valueOf(mGroupId), _next.getUUID()});
                if (modelMappingList.size() == 0) {
                    return;
                }

                ModelMapping modelMapping = modelMappingList.get(0);
                Product product = mProductController.findById(modelMapping.getClientSideUUID());
                IUnitController unitController = ControllerFactory.getUnitController(GlobalObjects.getInstance().getApplicationContext());
                Unit unit = unitController.getDefaultUnit();

                if (_next.getUnitUUID() != null) {
                    IModelMappingDbController unitMapping = ModelMappingDbFactory.getInstance().getSqliteUnitMappingController();
                    List<ModelMapping> unitMappingList = unitMapping.get(ModelMapping.COLUMN.SERVER_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID + " = ?",
                            new String[]{_next.getUnitUUID(), String.valueOf(mGroupId)});
                    if (unitMappingList.size() == 1) {
                        unit = unitController.findById(unitMappingList.get(0).getClientSideUUID());
                    }
                }
                // new entry
                Product productWithUpdate = mProductController.createProduct(_next.getName(),
                        unit, _next.getDefaultAmount(), _next.getStepAmount());
                productWithUpdate.mUUID = product.mUUID;
                Product updatedProduct = mProductController.modifyProduct(productWithUpdate);
                if (updatedProduct == null) {
                    Log.e(TAG, "onCompleted: update of product failed");
                    return;
                }

                modelMapping.setLastServerChanged(_next.getLastChanged());
                mProductModelMapping.update(modelMapping);
            }
            mTaskErrorLogDbController.remove(mCaseId);
        }

        @Override
        public void onError(Throwable _e) {

        }
    }
}
