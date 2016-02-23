package org.noorganization.instalistsynch.controller.network.model;

import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedInsertCallbackCompleted;

import java.util.List;

/**
 * Generic network interface to access models.
 * Created by Desnoo on 23.02.2016.
 */
public interface INetworkController<T> {

    /**
     * Get a list of items since a given time.
     *
     * @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the request should be done against.
     * @param _time      the time since when changes were made.
     * @param _authToken the auth token for the specified group.
     */
    void getList(IAuthorizedCallbackCompleted<List<T>> _callback, int _groupId,
            String _time, String _authToken);

    /**
     * Get a single item by its uuid.
     *
     * @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the request should be done against.
     * @param _uuid      the uuid of the list.
     * @param _authToken the auth token for the specified group.
     */
    void getItem(IAuthorizedCallbackCompleted<T> _callback, int _groupId,
            String _uuid, String _authToken);

    /**
     * Create a item on the server with the given params.
     *
     * @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the list should be created in.
     * @param _itemInfo  the itemInfo of the item to create it on the server.
     * @param _authToken the auth token for the specified group.
     */
    void createItem(IAuthorizedInsertCallbackCompleted<Void> _callback, int _groupId,
            T _itemInfo, String _authToken);

    /**
     * Update the given item to the server.
     *
     * @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the request should be done against.
     * @param _uuid      the uuid of the item.
     * @param _itemInfo  the itemInfo for the item to update it on the server.
     * @param _authToken the auth token for the specified group.
     */
    void updateItem(IAuthorizedCallbackCompleted<Void> _callback, int _groupId,
            String _uuid, T _itemInfo, String _authToken);

    /**
     * Delete the item with the specified uuid on the server.
     *
     * @param _callback  the callback where the result should be proceed.
     * @param _groupId   the groupid where the request should be done against.
     * @param _uuid      the uuid of the item to delete.
     * @param _authToken the auth token for the specified group.
     */
    void deleteItem(IAuthorizedCallbackCompleted<Void> _callback, int _groupId,
            String _uuid, String _authToken);

}
