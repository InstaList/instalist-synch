package org.noorganization.instalistsynch.controller.network.impl;

import android.util.Log;

import org.noorganization.instalist.comm.message.DeviceInfo;
import org.noorganization.instalist.comm.message.DeviceRegistration;
import org.noorganization.instalist.comm.message.GroupInfo;
import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.handler.AuthorizedCallbackHandler;
import org.noorganization.instalistsynch.controller.handler.UnauthorizedCallbackHandler;
import org.noorganization.instalistsynch.controller.network.IGroupNetworkController;
import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.network.api.authorized.IGroupApiService;
import org.noorganization.instalistsynch.utils.ApiUtils;

import java.util.List;

import de.greenrobot.event.EventBus;
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
    public void createGroup(ICallbackCompleted<GroupInfo> _callbackCompleted) {
        Call<GroupInfo> groupCall = ApiUtils.getInstance().getUnauthorizedInstantListApiService().registerGroup();
        groupCall.enqueue(new UnauthorizedCallbackHandler<>(_callbackCompleted, groupCall));
        Log.i(LOG_TAG, "createGroup: enquedCallback");
    }

    @Override
    public void joinGroup(ICallbackCompleted<DeviceInfo> _callback, String _groupAccessToken, String _deviceName, int _groupId, String _secret) {
        DeviceRegistration registration = new DeviceRegistration();
        registration.setGroupAuth(_groupAccessToken);
        registration.setName(_deviceName);
        registration.setSecret(_secret);

        Call<DeviceInfo> joinGroupCall = ApiUtils.getInstance().getUnauthorizedInstantListApiService().registerDevice(_groupId, registration);
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
    public void requestGroupAccessToken(IAuthorizedCallbackCompleted<GroupInfo> _callback, int _groupId, String _authToken) {
        Call<GroupInfo> requestAccessTokenCall = ApiUtils.getInstance()
                .getAuthorizedApiService(IGroupApiService.class, _authToken)
                .getGroupAccessKey(_groupId);
        requestAccessTokenCall.enqueue(new AuthorizedCallbackHandler<>(_groupId, _callback, requestAccessTokenCall));
        Log.i(LOG_TAG, "requestGroupAccessToken: enquedCallback");
    }

    @Override
    public void getGroupMembers(IAuthorizedCallbackCompleted<List<DeviceInfo>> _callback, int _groupId, String _authToken) {
        Call<List<DeviceInfo>> requestAccessTokenCall = ApiUtils.getInstance()
                .getAuthorizedApiService(IGroupApiService.class, _authToken)
                .getDevicesOfGroup(_groupId);
        requestAccessTokenCall.enqueue(new AuthorizedCallbackHandler<>(_groupId, _callback, requestAccessTokenCall));
        Log.i(LOG_TAG, "requestGroupAccessToken: enquedCallback");
    }

    @Override
    public void authorizeGroupMember(IAuthorizedCallbackCompleted<Void> _callback, GroupMember _groupMember, String _authToken) {
        if (_groupMember == null || _groupMember.hasNullFields()) {
            EventBus.getDefault().post(new ErrorMessageEvent(R.string.abc_error_member_not_existing));
            return;
        }
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setId(_groupMember.getGroupId());
        deviceInfo.setName(_groupMember.getName());
        deviceInfo.setAuthorized(_groupMember.getAccessRights().hasReadRight() && _groupMember.getAccessRights().hasWriteRight());

        Call<Void> authorizeGroupMember = ApiUtils.getInstance()
                .getAuthorizedApiService(IGroupApiService.class, _authToken)
                .updateDeviceOfGroup(_groupMember.getGroupId(), _groupMember.getDeviceId(), deviceInfo);
        authorizeGroupMember.enqueue(new AuthorizedCallbackHandler<>(_groupMember.getGroupId(), _callback, authorizeGroupMember));
        Log.i(LOG_TAG, "authorizeGroupMember: enquedCallback");
    }

}
