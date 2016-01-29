package org.noorganization.instalistsynch.controller.network.impl;

import android.util.Log;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.local.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.impl.LocalControllerFactory;
import org.noorganization.instalistsynch.controller.network.IGroupManager;
import org.noorganization.instalistsynch.events.ErrorMessage;
import org.noorganization.instalistsynch.model.Group;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.utils.GlobalObjects;

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

    @Override
    public void createGroup() {
        Call<String> groupCall = GlobalObjects.getInstance().getInstantListApiService().registerGroup();
        groupCall.enqueue(new RegisterGroupCallback());
    }

    @Override
    public void joinGroup(String _tmpGroupId) {
        SecureRandom secureRandom = GlobalObjects.getInstance().getSecureRandom();
        String secret = new BigInteger(196, secureRandom).toString(32);
        Group group = new Group(_tmpGroupId, secret);
        Call<String> auth = GlobalObjects.getInstance().getInstantListApiService().registerDevice(group);
        auth.enqueue(new RegisterDeviceCallback(secret));
    }

    @Override
    public void leaveGroup(GroupAuth _groupAuth) {
        // TODO impl
    }


    /**
     * Callback to the registerGroup action.
     */
    class RegisterGroupCallback implements Callback<String> {
        private final String LOG_TAG = RegisterGroupCallback.class.getSimpleName();

        @Override
        public void onResponse(Response<String> response) {
            if (!response.isSuccess()) {
                //noinspection finally
                try {
                    String msg = String.valueOf(response.code()).concat(" ").concat(response.errorBody().string());
                    EventBus.getDefault().post(new ErrorMessage(msg));
                    Log.i(LOG_TAG, "onResponse: server responded with ".concat(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new ErrorMessage(String.valueOf(response.code()).concat(" ")
                            .concat(GlobalObjects.getInstance()
                                    .getApplicationContext().getString(R.string.network_response_error))));
                    Log.e(LOG_TAG, "onResponse: Cannot load body of error message.", e.getCause());
                }
                return;
            }
            String tempCode = response.body();
            joinGroup(tempCode);
        }

        @Override
        public void onFailure(Throwable t) {
            EventBus.getDefault().post(new ErrorMessage(GlobalObjects.getInstance()
                    .getApplicationContext().getString(R.string.error_register)
                    .concat(t.getMessage())));
        }
    }

    /**
     * Callback to the registerDevice Callback.
     */
    class RegisterDeviceCallback implements Callback<String> {
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
        public void onResponse(Response<String> response) {
            if (!response.isSuccess()) {
                //noinspection finally
                try {
                    String msg = String.valueOf(response.code()).concat(" ").concat(response.errorBody().string());
                    EventBus.getDefault().post(new ErrorMessage(msg));
                    Log.i(LOG_TAG, "onResponse: server responded with ".concat(msg));
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
            GroupAuth groupAuth = new GroupAuth(response.body(), mSecret);
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
        }
    }
}
