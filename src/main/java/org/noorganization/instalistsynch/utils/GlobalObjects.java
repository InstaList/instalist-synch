package org.noorganization.instalistsynch.utils;

import android.content.Context;

import org.noorganization.instalist.enums.eControllerType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.presenter.ICategoryController;
import org.noorganization.instalist.presenter.IListController;
import org.noorganization.instalist.presenter.implementation.ControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.ITaskErrorLogDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.dba.impl.ModelMappingDbFactory;

import java.security.SecureRandom;
import java.util.Hashtable;
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

    public static Map<eControllerType, Object> sControllerMapping = new ConcurrentHashMap<>(eControllerType.ALL.ordinal());

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
        sControllerMapping.put(eControllerType.CATEGORY, ControllerFactory.getCategoryController(mContext));
        sControllerMapping.put(eControllerType.LIST, ControllerFactory.getListController(mContext));
        sControllerMapping.put(eControllerType.PRODUCT, ControllerFactory.getProductController(mContext));
        sControllerMapping.put(eControllerType.RECIPE, ControllerFactory.getRecipeController(mContext));
        sControllerMapping.put(eControllerType.TAG, ControllerFactory.getTagController(mContext));
        sControllerMapping.put(eControllerType.UNIT, ControllerFactory.getUnitController(mContext));
        sControllerMapping.put(eControllerType.MODEL_MAPPING, ModelMappingDbFactory.getInstance().getSqliteShoppingListMappingDbController());
        sControllerMapping.put(eControllerType.ERROR_LOG, LocalSqliteDbControllerFactory.getTaskErrorLogDbController(mContext));
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
