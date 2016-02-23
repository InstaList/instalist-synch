package org.noorganization.instalistsynch.controller.network.model;

import org.noorganization.instalist.comm.message.CategoryInfo;
import org.noorganization.instalist.comm.message.EntryInfo;
import org.noorganization.instalist.comm.message.IngredientInfo;
import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalist.comm.message.ProductInfo;
import org.noorganization.instalist.comm.message.RecipeInfo;
import org.noorganization.instalist.comm.message.TagInfo;
import org.noorganization.instalist.comm.message.TaggedProductInfo;
import org.noorganization.instalist.comm.message.UnitInfo;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalistsynch.controller.network.model.generic.ModelNetworkController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory to get all Synchronization Controller of all defined models.
 * Created by tinos_000 on 30.01.2016.
 */
public class RemoteModelAccessControllerFactory {

    private static RemoteModelAccessControllerFactory  sInstance;
    private        Map<eModelType, INetworkController> mMapping;

    /**
     * Get the instance of the factory.
     *
     * @return the instance of the factory.
     */
    public static RemoteModelAccessControllerFactory getInstance() {
        if (sInstance == null) {
            sInstance = new RemoteModelAccessControllerFactory();
        }
        return sInstance;
    }

    private RemoteModelAccessControllerFactory() {
        mMapping = new ConcurrentHashMap<>(eModelType.ALL.ordinal());
        init();
    }

    /**
     * Initializes the mapping map for the network interaction.
     */
    public void init() {
        mMapping.put(eModelType.CATEGORY,
                new ModelNetworkController<CategoryInfo>(eModelType.CATEGORY));
        mMapping.put(eModelType.LIST,
                new ModelNetworkController<ListInfo>(eModelType.LIST));
        mMapping.put(eModelType.LIST_ENTRY,
                new ModelNetworkController<EntryInfo>(eModelType.LIST_ENTRY));
        mMapping.put(eModelType.INGREDIENT,
                new ModelNetworkController<IngredientInfo>(eModelType.INGREDIENT));
        mMapping.put(eModelType.PRODUCT,
                new ModelNetworkController<ProductInfo>(eModelType.PRODUCT));
        mMapping.put(eModelType.RECIPE,
                new ModelNetworkController<RecipeInfo>(eModelType.RECIPE));
        mMapping.put(eModelType.TAG,
                new ModelNetworkController<TagInfo>(eModelType.TAG));
        mMapping.put(eModelType.TAGGED_PRODUCT,
                new ModelNetworkController<TaggedProductInfo>(eModelType.TAGGED_PRODUCT));
        mMapping.put(eModelType.UNIT,
                new ModelNetworkController<UnitInfo>(eModelType.UNIT));
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the List networkcontroller interface.
     */
    public INetworkController<ListInfo> getListNetworkController() {
        return mMapping.get(eModelType.LIST);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the category networkcontroller interface.
     */
    public INetworkController<CategoryInfo> getCategoryNetworkController() {
        return mMapping.get(eModelType.CATEGORY);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the tag networkcontroller interface.
     */
    public INetworkController<TagInfo> getTagNetworkController() {
        return mMapping.get(eModelType.TAG);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the recipe networkcontroller interface.
     */
    public INetworkController<RecipeInfo> getRecipeNetworkController() {
        return mMapping.get(eModelType.RECIPE);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the ingredient networkcontroller interface.
     */
    public INetworkController<IngredientInfo> getIngredientNetworkController() {
        return mMapping.get(eModelType.INGREDIENT);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the ingredient networkcontroller interface.
     */
    public INetworkController<EntryInfo> getListEntryNetworkController() {
        return mMapping.get(eModelType.LIST_ENTRY);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the product networkcontroller interface.
     */
    public INetworkController<ProductInfo> getProductNetworkController() {
        return mMapping.get(eModelType.PRODUCT);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the taggedproduct networkcontroller interface.
     */
    public INetworkController<TaggedProductInfo> getTaggedProductNetworkController() {
        return mMapping.get(eModelType.TAGGED_PRODUCT);
    }

    /**
     * Get the controller to do network interaction with the server.
     *
     * @return the unit networkcontroller interface.
     */
    public INetworkController<UnitInfo> getUnitNetworkController() {
        return mMapping.get(eModelType.UNIT);
    }
}
