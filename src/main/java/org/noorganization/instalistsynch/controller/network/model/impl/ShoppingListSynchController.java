package org.noorganization.instalistsynch.controller.network.model.impl;

import android.content.Context;
import android.util.Log;

import org.codehaus.jackson.map.util.ISO8601Utils;
import org.noorganization.instalist.model.ShoppingList;
import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.network.model.IModelSynchController;
import org.noorganization.instalistsynch.events.SynchronizationMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.network.response.ShoppingListResponse;
import org.noorganization.instalistsynch.network.api.IInstantListApiService;
import org.noorganization.instalistsynch.utils.GlobalObjects;
import org.noorganization.instalistsynch.utils.NetworkUtils;

import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Synchcontroller for {@link org.noorganization.instalist.model.ShoppingList}
 * Created by tinos_000 on 30.01.2016.
 */
public class ShoppingListSynchController implements IModelSynchController{
    private static final String LOG_TAG = ShoppingListSynchController.class.getSimpleName();

    private static ShoppingListSynchController sInstance;
    private IInstantListApiService mInstantListApiService;
    private EventBus mEventBus;

    public static ShoppingListSynchController getInstance(){
        if(sInstance == null)
            sInstance = new ShoppingListSynchController();
        return sInstance;
    }

    private ShoppingListSynchController() {
        mInstantListApiService = GlobalObjects.getInstance().getInstantListApiService();
        mEventBus = EventBus.getDefault();
    }

    @Override
    public void startSynchronization(GroupAuthAccess _groupAuthAccess) {
        // start a thread to synchronize
        Call<List<ShoppingListResponse>> shoppingListsSince = mInstantListApiService.getShoppingLists(_groupAuthAccess.getToken(),
                ISO8601Utils.format(_groupAuthAccess.getLastUpdated()));

        shoppingListsSince.enqueue(new GetShoppingListsSinceCallback());

    }

    @Override
    public void stopSynchronization() {

    }

    private class GetShoppingListsSinceCallback implements Callback<List<ShoppingListResponse>> {
        private final String LOG_TAG = GetShoppingListsSinceCallback.class.getSimpleName();

        @Override
        public void onResponse(Response<List<ShoppingListResponse>> _response) {
            if (NetworkUtils.isSuccessful(_response))
                return;

            List<ShoppingListResponse> responseBody = _response.body();
            for (ShoppingListResponse shoppingListResponse : responseBody) {
                // check if mapping already exists
                    // ja --> Änderungen seit letztem synch auf client (dazu letzten serversynch zeitpunkt nehmen und gucken ob client seitdem geändert?
                        // geändert?
                        // ja --> gucke ob der client neuer als server ist !
                            // ja zurückweisen oder nutzer fragen
                            // nein dann client updaten und werte setzen.
                    // else es ist new, check if id is unique

            }
        }

        @Override
        public void onFailure(Throwable t) {
            Log.e(LOG_TAG, "onFailure: Shoppinglistsynchronization connection went wrong.", t.getCause());
            mEventBus.post(new SynchronizationMessageEvent(ShoppingList.class, false, R.string.network_response_error));
        }
    }

}
