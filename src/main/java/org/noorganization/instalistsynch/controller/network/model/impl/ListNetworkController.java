package org.noorganization.instalistsynch.controller.network.model.impl;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;
import org.noorganization.instalistsynch.controller.handler.AuthorizedCallbackHandler;
import org.noorganization.instalistsynch.controller.handler.AuthorizedInsertCallbackHandler;
import org.noorganization.instalistsynch.controller.network.model.IListNetworkController;
import org.noorganization.instalistsynch.network.api.authorized.IListApiService;
import org.noorganization.instalistsynch.utils.ApiUtils;

import java.util.List;

import retrofit2.Call;

/**
 * Controller to fetch {@link org.noorganization.instalist.model.ShoppingList} items from the server.
 * Created by Desnoo on 12.02.2016.
 */
public class ListNetworkController implements IListNetworkController {

    private static ListNetworkController sInstance;

    public static ListNetworkController getInstance() {
        if (sInstance == null)
            sInstance = new ListNetworkController();
        return sInstance;
    }

    private ListNetworkController(){

    }

    @Override
    public void getLists(IAuthorizedCallbackCompleted<List<ListInfo>> _callback, int _groupId, String _time, String _authToken) {
        Call<List<ListInfo>> call = ApiUtils.getInstance().getAuthorizedApiService(IListApiService.class, _authToken).getShoppingLists(_groupId, _time);
        call.enqueue(new AuthorizedCallbackHandler<List<ListInfo>>(_groupId, _callback, call));
    }

    @Override
    public void getShoppingList(IAuthorizedCallbackCompleted<ListInfo> _callback, int _groupId, String _uuid, String _authToken) {
        Call<ListInfo> call = ApiUtils.getInstance().getAuthorizedApiService(IListApiService.class, _authToken).getShoppingList(_groupId, _uuid);
        call.enqueue(new AuthorizedCallbackHandler<ListInfo>(_groupId, _callback, call));
    }

    @Override
    public void createList(IAuthorizedInsertCallbackCompleted<Void> _callback, int _groupId, ListInfo _listInfo, String _authToken) {
        Call<Void> call = ApiUtils.getInstance().getAuthorizedApiService(IListApiService.class, _authToken).createShoppingList(_groupId, _listInfo);
        call.enqueue(new AuthorizedInsertCallbackHandler<Void>(_groupId, _callback, call));
    }

    @Override
    public void updateShoppingList(IAuthorizedCallbackCompleted<Void> _callback, int _groupId, String _uuid, ListInfo _listInfo, String _authToken) {
        Call<Void> call = ApiUtils.getInstance().getAuthorizedApiService(IListApiService.class, _authToken).updateShoppingList(_groupId, _uuid, _listInfo);
        call.enqueue(new AuthorizedCallbackHandler<Void>(_groupId, _callback, call));
    }

    @Override
    public void deleteShoppingList(IAuthorizedCallbackCompleted<Void> _callback, int _groupId, String _uuid, String _authToken) {
        Call<Void> call = ApiUtils.getInstance().getAuthorizedApiService(IListApiService.class, _authToken).deleteShoppingList(_groupId, _uuid);
        call.enqueue(new AuthorizedCallbackHandler<Void>(_groupId, _callback, call));
    }
}
