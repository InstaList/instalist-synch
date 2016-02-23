package org.noorganization.instalistsynch.controller.network.model;

import org.noorganization.instalist.comm.message.ListInfo;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;

import java.util.List;

/**
 * Interface that provides the access to the network interaction to the server.
 * Created by Desnoo on 13.02.2016.
 */
public interface IListNetworkController {

    /**
     * Get the lists since a given time.
     *
     * @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the request should be done against.
     * @param _time      the time since when changes were made.
     * @param _authToken the auth token for the specified group.
     */
    void getLists(IAuthorizedCallbackCompleted<List<ListInfo>> _callback, int _groupId, String _time, String _authToken);

    /**
     * Get a single list by its uuid.
     *
     * @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the request should be done against.
     * @param _uuid      the uuid of the list.
     * @param _authToken the auth token for the specified group.
     */
    void getShoppingList(IAuthorizedCallbackCompleted<ListInfo> _callback, int _groupId, String _uuid, String _authToken);

    /**
     * Create a list on the server with the given params.
     *  @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the list should be created in.
     * @param _listInfo  the listinfo of the list to create it on the server.
     * @param _authToken the auth token for the specified group.
     */
    void createList(IAuthorizedInsertCallbackCompleted<Void> _callback, int _groupId, ListInfo _listInfo, String _authToken);

    /**
     * Update the given list on the server.
     *
     * @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the request should be done against.
     * @param _uuid      the uuid of the list.
     * @param _listInfo  the listinfo of the list to update it on the server.
     * @param _authToken the auth token for the specified group.
     */
    void updateShoppingList(IAuthorizedCallbackCompleted<Void> _callback, int _groupId, String _uuid, ListInfo _listInfo, String _authToken);

    /**
     * Delete the list with the given uuid on the server.
     *
     * @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the request should be done against.
     * @param _uuid      the uuid of the list to delete.
     * @param _authToken the auth token for the specified group.
     */
    void deleteShoppingList(IAuthorizedCallbackCompleted<Void> _callback, int _groupId, String _uuid, String _authToken);
}
