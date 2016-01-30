package org.noorganization.instalistsynch.controller.network.impl;

import android.util.Log;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.local.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.impl.LocalControllerFactory;
import org.noorganization.instalistsynch.controller.network.IGroupManager;
import org.noorganization.instalistsynch.events.ErrorMessage;
import org.noorganization.instalistsynch.events.TokenMessage;
import org.noorganization.instalistsynch.model.Group;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.network.response.RetrofitAuthToken;
import org.noorganization.instalistsynch.model.network.response.RetrofitGroupAccessToken;
import org.noorganization.instalistsynch.model.network.response.RetrofitRegisterDevice;
import org.noorganization.instalistsynch.utils.GlobalObjects;
import org.noorganization.instalistsynch.utils.RFC2617Authorization;
import org.noorganization.instalistsynch.utils.StringUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

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
    }

    @Override
    public void createGroup() {
        Call<RetrofitGroupAccessToken> groupCall = GlobalObjects.getInstance().getInstantListApiService().registerGroup();
        groupCall.enqueue(new RegisterGroupCallback());
        Log.i(LOG_TAG, "createGroup: enquedCallback");
    }

    @Override
    public void joinGroup(String _tmpGroupId) {
        Log.i(LOG_TAG, "joinGroup: " + _tmpGroupId);
        SecureRandom secureRandom = GlobalObjects.getInstance().getSecureRandom();
        String secret = new BigInteger(196, secureRandom).toString(32);
        Group group = new Group(_tmpGroupId, secret);
        Call<RetrofitRegisterDevice> deviceIdGen = GlobalObjects.getInstance().getInstantListApiService().registerDevice(group);
        deviceIdGen.enqueue(new RegisterDeviceCallback(secret));
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
        authTokenReq.enqueue(new GetAuthTokenCallback());
    }

    /**
     * Callback to the registerGroup action.
     */
    private class RegisterGroupCallback implements Callback<RetrofitGroupAccessToken> {
        private final String LOG_TAG = RegisterGroupCallback.class.getSimpleName();

        @Override
        public void onResponse(Response<RetrofitGroupAccessToken> response) {
            if (!response.isSuccess()) {
                //noinspection finally
                try {
                    String msg = String.valueOf(response.code()).concat(" ").concat(response.errorBody().string());
                    EventBus.getDefault().post(new ErrorMessage(msg));
                    Log.e(LOG_TAG, "onResponse: server responded with ".concat(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new ErrorMessage(String.valueOf(response.code()).concat(" ")
                            .concat(GlobalObjects.getInstance()
                                    .getApplicationContext().getString(R.string.network_response_error))));
                    Log.e(LOG_TAG, "onResponse: Cannot load body of error message.", e.getCause());
                }
                return;
            }
            Log.i(LOG_TAG, "Response: " + response.body());

            joinGroup(response.body().groupid);
        }

        @Override
        public void onFailure(Throwable t) {
            EventBus.getDefault().post(new ErrorMessage(GlobalObjects.getInstance()
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

        /**
         * Default Constructor with the secret as param.
         *
         * @param _secret the secret to this device.
         */
        public RegisterDeviceCallback(String _secret) {
            mSecret = _secret;
        }

        @Override
        public void onResponse(Response<RetrofitRegisterDevice> response) {
            if (!response.isSuccess()) {
                //noinspection finally
                try {
                    String msg = String.valueOf(response.code()).concat(" ").concat(response.errorBody().string());
                    EventBus.getDefault().post(new ErrorMessage(msg));
                    Log.e(LOG_TAG, "onResponse: server responded with ".concat(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new ErrorMessage(String.valueOf(response.code())
                            .concat(" ").concat(GlobalObjects.getInstance().getApplicationContext()
                                    .getString(R.string.network_response_error))));
                    Log.e(LOG_TAG, "onResponse: Cannot load body of error message.", e.getCause());
                }
                return;
            }

            Log.i(LOG_TAG, "Response: " + response.body());
            String deviceId = response.body().deviceid;

            // insert groupAuth to db
            GroupAuth groupAuth = new GroupAuth(deviceId, mSecret);
            // possible security breach!
            IGroupAuthDbController authDbController = LocalControllerFactory
                    .getDefaultAuthController(GlobalObjects.getInstance().getApplicationContext());
            if (!authDbController.hasUniqueId(groupAuth)) {
                // retry at this place
                EventBus.getDefault().post(new ErrorMessage(GlobalObjects.getInstance()
                        .getApplicationContext()
                        .getString(R.string.server_sent_not_unique_id_retry)));
                return;
            }
            authDbController.insertRegisteredGroup(groupAuth);
            // EventBus.getDefault().post(new NewGroupAuthMessage(groupAuth));
        }

        @Override
        public void onFailure(Throwable t) {
            EventBus.getDefault().post(new ErrorMessage(GlobalObjects.getInstance()
                    .getApplicationContext().getString(R.string.error_join).concat(t.getMessage())));
            Log.e(LOG_TAG, "onFailure: ", t.getCause());
        }
    }

    private class GetAuthTokenCallback implements Callback<RetrofitAuthToken> {
        private final String LOG_TAG = GetAuthTokenCallback.class.getSimpleName();

        @Override
        public void onResponse(Response<RetrofitAuthToken> response) {
            if (!response.isSuccess()) {
                //noinspection finally
                try {
                    String msg = String.valueOf(response.code()).concat(" ").concat(response.errorBody().string());
                    EventBus.getDefault().post(new ErrorMessage(msg));
                    Log.e(LOG_TAG, "onResponse: server responded with ".concat(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new ErrorMessage(String.valueOf(response.code())
                            .concat(" ").concat(GlobalObjects.getInstance().getApplicationContext()
                                    .getString(R.string.network_response_error))));
                    Log.e(LOG_TAG, "onResponse: Cannot load body of error message.", e.getCause());
                }
                return;
            }

            String token = response.body().token;
            if (token != null) {
                EventBus.getDefault().post(new TokenMessage(token));
            } else {
                Log.e(LOG_TAG, "onResponse: Token can not be parsed from response.");
                EventBus.getDefault().post(new ErrorMessage(GlobalObjects.getInstance()
                        .getApplicationContext().getString(R.string.error_response_not_parseable)));
            }
        }

        @Override
        public void onFailure(Throwable t) {
            EventBus.getDefault().post(new ErrorMessage(GlobalObjects.getInstance()
                    .getApplicationContext().getString(R.string.error_join).concat(t.getMessage())));
            Log.e(LOG_TAG, "onFailure: ", t.getCause());
        }
    }
}
