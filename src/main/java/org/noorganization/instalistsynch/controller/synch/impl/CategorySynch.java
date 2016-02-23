package org.noorganization.instalistsynch.controller.synch.impl;

import android.content.Context;

import org.noorganization.instalist.model.Category;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.synch.ISynch;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.util.Date;
import java.util.List;

/**
 * The synchronization for the cateogries.
 * Created by Desnoo on 24.02.2016.
 */
public class CategorySynch implements ISynch {

    private ISessionController        mSessionController;
    private ICategoryController       mCategoryController;
    private IModelMappingDbController mCategoryModelMappingController;

    public CategorySynch() {
        Context context = GlobalObjects.getInstance().getApplicationContext();
        mSessionController = InMemorySessionController.getInstance();
        mCategoryController = ControllerFactory.getCategoryController(context);
        mCategoryModelMappingController =
                ModelMappingDbFactory.getInstance().getSqliteCategoryMappingDbController();
    }

    @Override
    public void indexLocalEntries(int _groupId) {
        List<Category> categoryList = mCategoryController.getAllCategories();
        ModelMapping   categoryMapping;

        for (Category category : categoryList) {
            categoryMapping =
                    new ModelMapping(null, _groupId, null, category.mUUID, null, new Date());
            mCategoryModelMappingController.insert(categoryMapping);
        }
    }

    @Override
    public void synchronizeLocalToNetwork(int _groupId, String _lastUpdate) {
        String authToken = mSessionController.getToken(_groupId);
        if (authToken == null) {
            // todo do some caching of this action
            return;
        }
    }

    @Override
    public void refreshLocalMapping(int _groupId, Date _sinceTime) {

    }

    @Override
    public void synchGroupFromNetwork(int _groupId, Date _sinceTime) {

    }

    @Override
    public void resolveConflict(int _conflictId, int _resolveAction) {

    }
}
