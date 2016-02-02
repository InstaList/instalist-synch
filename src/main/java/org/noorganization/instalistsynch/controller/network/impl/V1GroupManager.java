package org.noorganization.instalistsynch.controller.network.impl;

import android.content.Context;
import android.util.Log;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.local.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.IGroupMemberDbController;
import org.noorganization.instalistsynch.controller.local.impl.LocalControllerFactory;
import org.noorganization.instalistsynch.controller.network.IGroupManager;
import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent;
import org.noorganization.instalistsynch.events.GroupMemberListMessageEvent;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.model.Group;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.network.response.GroupMemberRetrofit;
import org.noorganization.instalistsynch.model.network.response.RetrofitAuthToken;
import org.noorganization.instalistsynch.model.network.response.RetrofitGroupAccessToken;
import org.noorganization.instalistsynch.model.network.response.RetrofitRegisterDevice;
import org.noorganization.instalistsynch.utils.GlobalObjects;
import org.noorganization.instalistsynch.utils.NetworkUtils;
import org.noorganization.instalistsynch.utils.RFC2617Authorization;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Group manager for api version 1.
 * Created by tinos_000 on 29.01.2016.
 */
public class V1GroupManager implements IGroupManager {
    private static final String LOG_TAG = V1GroupManager.class.getSimpleName();

    /**
     * The instance of this class.
     */
    private static V1GroupManager sInstance;

    private Context mContext;

    /**
     * Get instance of this singleton.
     *
     * @return the only instance.
     */
    public static V1GroupManager getInstance() {
        if (sInstance == null) {
            sInstance = new V1GroupManager();
        }
        return sInstance;
    }

    /**
     * Default private constructor.
     */
    private V1GroupManager() {
        mContext = GlobalObjects.getInstance().getApplicationContext();
    }

    @Override
    public void createGroup(String _deviceName) {
        if (LocalControllerFactory.getDefaultAuthController(mContext).hasOwnLocalGroup()) {
            EventBus.getDefault().post(new ErrorMessageEvent(mContext.getString(R.string.abc_local_group_exists)));
            return;
        }
        Call<RetrofitGroupAccessToken> groupCall = GlobalObjects.getInstance().getInstantListApiService().registerGroup();
        groupCall.enqueue(new RegisterGroupCallback(_deviceName));
        Log.i(LOG_TAG, "createGroup: enquedCallback");
    }

    @Override
    public void joinGroup(String _tmpGroupId, String _deviceName, boolean _isLocal) {
        Log.i(LOG_TAG, "joinGroup: " + _tmpGroupId);
        SecureRandom secureRandom = GlobalObjects.getInstance().getSecureRandom();
        String secret = new BigInteger(196, secureRandom).toString(32);
        Group group = new Group(_tmpGroupId, secret, _deviceName);
        Call<RetrofitRegisterDevice> deviceIdGen = GlobalObjects.getInstance().getInstantListApiService().registerDevice(group);
        deviceIdGen.enqueue(new RegisterDeviceCallback(secret, _deviceName, _isLocal));
    }

    @Override
    public void leaveGroup(GroupAuth _groupAuth) {
        // TODO impl
    }

    @Override
    public void requestAuthToken(GroupAuth _groupAuth) {
        Log.i(LOG_TAG, "requestAuthToken: for " + _groupAuth.getDeviceId());
        Call<RetrofitAuthToken> authTokenReq = GlobalObjects.getInstance()
                .getInstantListApiService()
                .token(RFC2617Authorization
                        .generate(_groupAuth.getDeviceId(), _groupAuth.getSecret()));
        authTokenReq.enqueue(new GetAuthTokenCallback(_groupAuth));
    }

    @Override
    public void requestGroupAccessToken(String _authToken) {
        Log.i(LOG_TAG, "requestGroupAccessToken: for " + _authToken);
        Call<RetrofitGroupAccessToken> accessTokenRequest = GlobalObjects.getInstance().getInstantListApiService().getGroupAccessKey(_authToken);
        accessTokenRequest.enqueue(new GetTemporaryGroupAccessKey());
    }

    @Override
    public void getGroupMembers(String _authToken) {
        Call<List<GroupMemberRetrofit>> groupMemberRetrofitCall = GlobalObjects.getInstance().getInstantListApiService().getDevicesOfGroup(_authToken);
        groupMemberRetrofitCall.enqueue(new GetDevicesOfGroup());
    }

    @Override
    public void updateGroupMembers() {

    }

    @Override
    public void deleteGroupMember(GroupMember _groupMember) {

    }

    @Override
    public void approveGroupMember(GroupMember _groupMember) {

    }

    /**
     * Callback to the registerGroup action.
     */
    private class RegisterGroupCallback implements Callback<RetrofitGroupAccessToken> {
        private final String LOG_TAG = RegisterGroupCallback.class.getSimpleName();
        private String mDeviceName;

        public RegisterGroupCallback(String _deviceName) {
            mDeviceName = _deviceName;
        }

        @Override
        public void onResponse(Response<RetrofitGroupAccessToken> response) {
            if (!NetworkUtils.isSuccessful(response, "/register_group"))
                return;
            Log.i(LOG_TAG, "Response: " + response.body());
            joinGroup(response.body().groupid, mDeviceName, true);
        }

        @Override
        public void onFailure(Throwable t) {
            EventBus.getDefault().post(new ErrorMessageEvent(GlobalObjects.getInstance()
                    .getApplicationContext().getString(R.string.error_register)
                    .concat(t.getMessage())));
            Log.e(LOG_TAG, "onFailure: ", t.getCause());
        }
    }

    /**
     * Callback to the registerDevice Callback.
     */
    private class RegisterDeviceCallback implements Callback<RetrofitRegisterDevice> {
        private final String LOG_TAG = RegisterDeviceCallback.class.getSimpleName();

        private String mSecret;
        private String mDeviceName;
        private boolean mIsLocal;

        /**
         * Default Constructor with the secret as param.
         *
         * @param _secret the secret to this device.
         */
        public RegisterDeviceCallback(String _secret, String _deviceName, boolean _isLocal) {
            mSecret = _secret;
            mDeviceName = _deviceName;
            mIsLocal = _isLocal;
        }

        @Override
        public void onResponse(Response<RetrofitRegisterDevice> response) {
            if (!NetworkUtils.isSuccessful(response, "/register_device"))
                return;

            Log.i(LOG_TAG, "Response: " + response.body());
            String deviceId = response.body().deviceid;

            // insert groupAuth to db
            GroupAuth groupAuth = new GroupAuth(deviceId, mSecret, mDeviceName, mIsLocal);
            // possible security breach!
            IGroupAuthDbController authDbController = LocalControllerFactory
                    .getDefaultAuthController(mContext);
            IGroupMemberDbController groupMemberDbController = LocalControllerFactory.getGroupMemberDbController(mContext);

            if (!authDbController.hasUniqueId(groupAuth)) {
                // retry at this place
                EventBus.getDefault().post(new ErrorMessageEvent(GlobalObjects.getInstance()
                        .getApplicationContext()
                        .getString(R.string.server_sent_not_unique_id_retry)));
                return;
            }
            authDbController.insertRegisteredGroup(groupAuth);
            groupMemberDbController.insert(new GroupMember(null, groupAuth.getDeviceId(), groupAuth.getDeviceId(), groupAuth.getDeviceName(), mIsLocal));
            // EventBus.getDefault().post(new NewGroupAuthMessage(groupAuth));
            requestAuthToken(groupAuth);
        }

        @Override
        public void onFailure(Throwable t) {
            EventBus.getDefault().post(new ErrorMessageEvent(GlobalObjects.getInstance()
                    .getApplicationContext().getString(R.string.error_join).concat(t.getMessage())));
            Log.e(LOG_TAG, "onFailure: ", t.getCause());
        }
    }

    /**
     * Callback  from the authtoken get request.
     */
    private class GetAuthTokenCallback implements Callback<RetrofitAuthToken> {
        private final String LOG_TAG = GetAuthTokenCallback.class.getSimpleName();
        private GroupAuth mGroupAuth;

        public GetAuthTokenCallback(GroupAuth groupAuth) {
            mGroupAuth = groupAuth;
        }

        @Override
        public void onResponse(Response<RetrofitAuthToken> response) {
            if (!NetworkUtils.isSuccessful(response, "/token"))
                return;

            String token = response.body().token;
            if (token == null) {
                Log.e(LOG_TAG, "onResponse: Token can not be parsed from response.");
                EventBus.getDefault().post(new ErrorMessageEvent(GlobalObjects.getInstance()
                        .getApplicationContext().getString(R.string.error_response_not_parseable)));
                return;
            }
            IGroupAuthAccessDbController authAccessDbController = LocalControllerFactory
                    .getSqliteAuthAccessController(mContext);

            boolean insertionSuccess = false;
            if (authAccessDbController.hasIdInDatabase(mGroupAuth.getDeviceId())) {
                boolean updateSuccess = authAccessDbController.updateToken(mGroupAuth.getDeviceId(), token);
                insertionSuccess = updateSuccess;
                if (!updateSuccess) {
                    Log.e(LOG_TAG, "onResponse: insertion of groupAuthAccess element went wrong");

                }
            } else {
                GroupAuthAccess access = new GroupAuthAccess(mGroupAuth.getDeviceId(), token);
                access.setLastTokenRequest(new Date());
                access.setLastUpdated(new Date(System.currentTimeMillis() - 1000000L));
                int ret = authAccessDbController.insert(access);
                insertionSuccess = ret == IGroupAuthAccessDbController.INSERTION_CODE.CORRECT;
                if (ret != IGroupAuthAccessDbController.INSERTION_CODE.CORRECT) {
                    Log.e(LOG_TAG, "onResponse: insertion of groupAuthAccess element went wrong");
                    return;
                }
            }

            if (insertionSuccess) {
                EventBus.getDefault().post(new TokenMessageEvent(token));
            } else {
                EventBus.getDefault().post(new ErrorMessageEvent(mContext.getString(R.string.internal_db_error)));
            }


        }


        @Override
        public void onFailure(Throwable t) {
            EventBus.getDefault().post(new ErrorMessageEvent(GlobalObjects.getInstance()
                    .getApplicationContext().getString(R.string.error_join).concat(t.getMessage())));
            Log.e(LOG_TAG, "onFailure: ", t.getCause());
        }
    }


    private class GetTemporaryGroupAccessKey implements Callback<RetrofitGroupAccessToken> {

        @Override
        public void onResponse(Response<RetrofitGroupAccessToken> response) {
            if (!NetworkUtils.isSuccessful(response, "user/group/access_key"))
                return;

            EventBus.getDefault().post(new GroupAccessTokenMessageEvent(response.body().groupid));
        }

        @Override
        public void onFailure(Throwable t) {
            EventBus.getDefault().post(new ErrorMessageEvent(GlobalObjects.getInstance()
                    .getApplicationContext().getString(R.string.error_temporary_group_access).concat(t.getMessage())));
            Log.e(LOG_TAG, "onFailure: ", t.getCause());
        }
    }

    /**
     * Query to get all devices of the group.
     */
    private class GetDevicesOfGroup implements Callback<List<GroupMemberRetrofit>> {

        @Override
        public void onResponse(Response<List<GroupMemberRetrofit>> response) {
            if (!NetworkUtils.isSuccessful(response, "user/group/access_key"))
                return;

            List<GroupMember> groupMemberList = new ArrayList<>(response.body().size());
            for (GroupMemberRetrofit groupMemberRetrofit : response.body()) {
                groupMemberList.add(new GroupMember(null, groupMemberRetrofit.id, null, groupMemberRetrofit.name, groupMemberRetrofit.authorized));
            }

            GroupMemberListMessageEvent msg = new GroupMemberListMessageEvent(groupMemberList, groupMemberList.get(0).getDeviceId());
            EventBus.getDefault().post(msg);
        }

        @Override
        public void onFailure(Throwable t) {
            EventBus.getDefault().post(new ErrorMessageEvent(GlobalObjects.getInstance()
                    .getApplicationContext().getString(R.string.error_devices_group).concat(t.getMessage())));
            Log.e(LOG_TAG, "onFailure: ", t.getCause());
        }
    }
}
