package org.noorganization.instalistsynch.controller.network.impl;

import android.util.Log;

import org.noorganization.instalistsynch.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.handler.AuthorizedCallbackHandler;
import org.noorganization.instalistsynch.controller.handler.UnauthorizedCallbackHandler;
import org.noorganization.instalistsynch.controller.network.IGroupNetworkController;
import org.noorganization.instalistsynch.model.Group;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.network.response.GroupAccessKey;
import org.noorganization.instalistsynch.model.network.response.GroupMemberRetrofit;
import org.noorganization.instalistsynch.model.network.response.GroupResponse;
import org.noorganization.instalistsynch.model.network.response.RegisterDeviceResponse;
import org.noorganization.instalistsynch.network.api.authorized.IGroupApiService;
import org.noorganization.instalistsynch.utils.ApiUtils;

import java.util.List;

import retrofit2.Call;

/**
 * Group manager that handles network request for api version 1. It uses callbacks to notify the caller.
 * Take in mind! to not overdo callbacks inside the other callbacks ;)
 * Created by tinos_000 on 08.02.2016.
 */
public class V1GroupNetworkController implements IGroupNetworkController {
    private static final String LOG_TAG = V1GroupNetworkController.class.getSimpleName();

    /**
     * Instance member.
     */
    private static V1GroupNetworkController sInstance;

    /**
     * Get the instance of this V1GroupManagerNetwork.
     *
     * @return the single instance of this class.
     */
    public static V1GroupNetworkController getInstance() {
        if (sInstance == null) {
            sInstance = new V1GroupNetworkController();
        }
        return sInstance;
    }


    @Override
    public void createGroup(ICallbackCompleted<GroupResponse> _callbackCompleted) {
        Call<GroupResponse> groupCall = ApiUtils.getInstance().getUnauthorizedInstantListApiService().registerGroup();
        groupCall.enqueue(new UnauthorizedCallbackHandler<>(_callbackCompleted, groupCall));
        Log.i(LOG_TAG, "createGroup: enquedCallback");
    }

    @Override
    public void joinGroup(ICallbackCompleted<RegisterDeviceResponse> _callback, String _groupAccessToken, String _deviceName, int _groupId, String _secret) {
        Group group = new Group(_groupAccessToken, _secret, _deviceName);
        Call<RegisterDeviceResponse> joinGroupCall = ApiUtils.getInstance().getUnauthorizedInstantListApiService().registerDevice(_groupId, group);
        joinGroupCall.enqueue(new UnauthorizedCallbackHandler<>(_callback, joinGroupCall));
        Log.i(LOG_TAG, "createGroup: enquedCallback");

    }

    @Override
    public void deleteMemberOfGroup(IAuthorizedCallbackCompleted<Void> _callback, String _authToken, int _groupId, int _deviceId) {
        Call<Void> deleteCall = ApiUtils.getInstance().getAuthorizedApiService(IGroupApiService.class, _authToken).deleteDevicesOfGroup(_groupId, _deviceId);
        deleteCall.enqueue(new AuthorizedCallbackHandler<>(_groupId, _callback, deleteCall));
        Log.i(LOG_TAG, "deleteMemberOfGroup: enquedCallback");
    }

    @Override
    public void requestGroupAccessToken(IAuthorizedCallbackCompleted<GroupAccessKey> _callback, int _groupId, String _authToken) {
        Call<GroupAccessKey> requestAccessTokenCall = ApiUtils.getInstance()
                .getAuthorizedApiService(IGroupApiService.class, _authToken)
                .getGroupAccessKey(_groupId);
        requestAccessTokenCall.enqueue(new AuthorizedCallbackHandler<>(_groupId, _callback, requestAccessTokenCall));
        Log.i(LOG_TAG, "requestGroupAccessToken: enquedCallback");
    }

    @Override
    public void getGroupMembers(IAuthorizedCallbackCompleted<List<GroupMemberRetrofit>> _callback, int _groupId, String _authToken) {
        Call<List<GroupMemberRetrofit>> requestAccessTokenCall = ApiUtils.getInstance()
                .getAuthorizedApiService(IGroupApiService.class, _authToken)
                .getDevicesOfGroup(_groupId);
        requestAccessTokenCall.enqueue(new AuthorizedCallbackHandler<>(_groupId, _callback, requestAccessTokenCall));
        Log.i(LOG_TAG, "requestGroupAccessToken: enquedCallback");
    }

    @Override
    public void authorizeGroupMember(IAuthorizedCallbackCompleted<Void> _callback, GroupMember _groupMember, String _authToken) {

        GroupMemberRetrofit groupMemberRetrofit = new GroupMemberRetrofit();
        groupMemberRetrofit.authorized = _groupMember.getAccessRights().hasReadRight() && _groupMember.getAccessRights().hasWriteRight();
        groupMemberRetrofit.id = _groupMember.getGroupId();
        groupMemberRetrofit.name = _groupMember.getName();

        Call<Void> authorizeGroupMember = ApiUtils.getInstance()
                .getAuthorizedApiService(IGroupApiService.class, _authToken)
                .updateDeviceOfGroup(_groupMember.getGroupId(), _groupMember.getDeviceId(), groupMemberRetrofit);
        authorizeGroupMember.enqueue(new AuthorizedCallbackHandler<>(_groupMember.getGroupId(), _callback, authorizeGroupMember));
        Log.i(LOG_TAG, "authorizeGroupMember: enquedCallback");
    }

}
