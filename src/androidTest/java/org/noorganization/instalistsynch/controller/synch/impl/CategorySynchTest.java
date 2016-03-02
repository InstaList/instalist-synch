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
import android.test.AndroidTestCase;

import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.network.AuthNetworkControllerFactory;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.util.Date;
import java.util.List;

/**
 * Created by Desnoo on 26.02.2016.
 */
public class CategorySynchTest extends AndroidTestCase {

    private Context mContext;
    private ICategoryController mCategoryController;
    private Category mCategory;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();

        SynchDbHelper dbHelper = new SynchDbHelper(mContext);
        GlobalObjects.getInstance().setApplicationContext(mContext);
        mCategoryController = ControllerFactory.getCategoryController(mContext);

        for (Category category : mCategoryController.getAllCategories()) {
            mCategoryController.removeCategory(category);
        }


        mCategory = mCategoryController.createCategory("TEST_category");
        assertNotNull(mCategory);

        GroupAccess groupAccess = new GroupAccess(1, "afijodijfoaenn");
        groupAccess.setLastUpdateFromClient(new Date());
        groupAccess.setLastTokenRequest(new Date());
        groupAccess.setLastUpdateFromServer(new Date());
        GroupAuth   groupAuth   = new GroupAuth(1, 1, "asidjasodij", "test", true);
        assertTrue(LocalSqliteDbControllerFactory.getGroupAuthDbController(mContext).insertRegisteredGroup(groupAuth));
        int i = LocalSqliteDbControllerFactory.getAuthAccessDbController(mContext).insert(groupAccess);
        //assertEquals(IGroupAuthAccessDbController.INSERTION_CODE.CORRECT, i);

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();

        List<ModelMapping> additionalCatMapping = modelMappingDbController.get(
                null, null);
        for (ModelMapping modelMapping : additionalCatMapping) {
            assertTrue(modelMappingDbController.delete(modelMapping));
        }

        mCategoryController.removeCategory(mCategory);
        assertEquals(1, LocalSqliteDbControllerFactory.getGroupAuthDbController(mContext).removeRegisteredGroup(1));
    }

    public void testIndexLocalEntries() throws Exception {
        CategorySynch categorySynch = new CategorySynch(eModelType.CATEGORY);
        categorySynch.indexLocalEntries(-1);
        IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        List<ModelMapping> categoryList = modelMappingDbController.get(
                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID
                        + " = ? ", new String[]{mCategory.mUUID, String.valueOf(-1)});

        assertEquals(1, categoryList.size());
        ModelMapping categoryMapping = categoryList.get(0);
        assertEquals(mCategory.mUUID, categoryMapping.getClientSideUUID());
        assertTrue(modelMappingDbController.delete(categoryMapping));
    }

    public void testIndexLocal() throws Exception {
        CategorySynch categorySynch = new CategorySynch(eModelType.CATEGORY);
        categorySynch.indexLocal(-1, new Date(0));
        IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        List<ModelMapping> categoryList = modelMappingDbController.get(
                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID
                        + " = ? ", new String[]{mCategory.mUUID, String.valueOf(-1)});

        assertEquals(1, categoryList.size());
        ModelMapping categoryMapping = categoryList.get(0);
        assertEquals(mCategory.mUUID, categoryMapping.getClientSideUUID());
        assertTrue(modelMappingDbController.delete(categoryMapping));
    }

    public void testAddGroupToMapping() throws Exception {
        CategorySynch categorySynch = new CategorySynch(eModelType.CATEGORY);
        categorySynch.indexLocalEntries(1);

        categorySynch.addGroupToMapping(2, mCategory.mUUID);
        IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        List<ModelMapping> categoryList = modelMappingDbController.get(
                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID
                        + " = ? ", new String[]{mCategory.mUUID, String.valueOf(2)});

        assertEquals(1, categoryList.size());
        ModelMapping categoryMapping = categoryList.get(0);
        assertEquals(mCategory.mUUID, categoryMapping.getClientSideUUID());
        assertTrue(modelMappingDbController.delete(categoryMapping));

    }

    public void testRemoveGroupFromMapping() throws Exception {
        CategorySynch categorySynch = new CategorySynch(eModelType.CATEGORY);
        categorySynch.indexLocal(1, new Date(0));

        categorySynch.addGroupToMapping(2, mCategory.mUUID);
        IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        List<ModelMapping> categoryList = modelMappingDbController.get(
                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID
                        + " = ? ", new String[]{mCategory.mUUID, String.valueOf(2)});

        assertEquals(1, categoryList.size());

        categorySynch.removeGroupFromMapping(2, mCategory.mUUID);
        modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        categoryList = modelMappingDbController.get(
                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID
                        + " = ? ", new String[]{mCategory.mUUID, String.valueOf(2)});

        assertEquals(0, categoryList.size());
    }

    public void testSynchLocalToNetwork() throws Exception {
        /*CategorySynch categorySynch = new CategorySynch(eModelType.CATEGORY);
        categorySynch.indexLocalEntries(1);

        categorySynch.synchLocalToNetwork(1, new Date());

        wait(3000);
        IModelMappingDbController modelMappingDbController = ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
        List<ModelMapping> categoryList = modelMappingDbController.get(
                ModelMapping.COLUMN.CLIENT_SIDE_UUID + " LIKE ? AND " + ModelMapping.COLUMN.GROUP_ID
                        + " = ? ", new String[]{mCategory.mUUID, String.valueOf(1)});

        assertEquals(1, categoryList.size());
        ModelMapping modelMapping = categoryList.get(0);
        assertNotNull(modelMapping.getServerSideUUID());*/
    }

    public void testSynchNetworkToLocal() throws Exception {

    }

    public void testResolveConflict() throws Exception {

    }
}