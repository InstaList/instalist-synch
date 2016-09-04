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

package org.noorganization.instalistsynch.utils;

import android.content.Context;

import org.noorganization.instalist.types.ControllerType;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A Global object singleton. Holds objects that only must created once.
 * Created by tinos_000 on 29.01.2016.
 */
public class GlobalObjects {

    /**
     * Map the group id to a boolean that indicates if the token is currently new requested.
     */
    public static Map<Integer, Boolean> sCallMapping = new ConcurrentHashMap<>(2);

    /**
     * Map the model type to the corresponding controller.
     */

    public static Map<Integer, Object> sControllerMapping = new ConcurrentHashMap<>(ControllerType.ALL);

    /**
     * Instance of this class.
     */
    private static GlobalObjects sInstance;


    /**
     * The securerandom instance.
     */
    private SecureRandom mSecureRandom;

    /**
     * Context of the app.
     */
    private Context mContext;

    /**
     * Get the instance of this class.
     *
     * @return this GlobalObjects instance.
     */
    public static GlobalObjects getInstance() {
        if (sInstance == null) {
            sInstance = new GlobalObjects();
        }
        return sInstance;
    }

    /**
     * Set the application Context.
     *
     * @param _context the context of the application.
     */
    public void setApplicationContext(Context _context) {
        mContext = _context;
    }

    /**
     * Get the context of the application.
     *
     * @return the context of the application.
     */
    public Context getApplicationContext() {
        return mContext;
    }

    /**
     * Default private constructor.
     */
    private GlobalObjects() {
        mSecureRandom = new SecureRandom();
    }

    /**
     * Initializes all controllers and map them to a Hashmap.
     * As always first set the context.
     */
    public void initController() {
        sControllerMapping.put(ControllerType.CATEGORY, ControllerFactory.getCategoryController(mContext));
        sControllerMapping.put(ControllerType.LIST, ControllerFactory.getListController(mContext));
        sControllerMapping.put(ControllerType.PRODUCT, ControllerFactory.getProductController(mContext));
        sControllerMapping.put(ControllerType.RECIPE, ControllerFactory.getRecipeController(mContext));
        sControllerMapping.put(ControllerType.TAG, ControllerFactory.getTagController(mContext));
        sControllerMapping.put(ControllerType.UNIT, ControllerFactory.getUnitController(mContext));
        sControllerMapping.put(ControllerType.MODEL_MAPPING, ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController());
        sControllerMapping.put(ControllerType.ERROR_LOG, LocalSqliteDbControllerFactory.getTaskErrorLogDbController(mContext));
    }

    /**
     * Get an instance of SecureRandom.
     *
     * @return the secureRandom instance.
     */
    public SecureRandom getSecureRandom() {
        return mSecureRandom;
    }
}
