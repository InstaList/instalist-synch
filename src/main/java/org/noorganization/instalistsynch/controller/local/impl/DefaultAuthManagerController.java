package org.noorganization.instalistsynch.controller.local.impl;

import android.util.Log;

import org.noorganization.instalistsynch.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.local.IAuthManagerController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.impl.NetworkControllerFactory;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.network.response.RetrofitAuthToken;
import org.noorganization.instalistsynch.utils.GlobalObjects;

/**
 * The DefaultAuthManagerController implementation. It handles the Authentification of a user.
 * Created by tinos_000 on 08.02.2016.
 */
public class DefaultAuthManagerController implements IAuthManagerController {
    private static final String LOG_TAG = DefaultAuthManagerController.class.getSimpleName();

    private static DefaultAuthManagerController sInstance;
    private ISessionController mSessionController;
    private IGroupAuthAccessDbController dbController;

    /**
     * Get the DefaultAuthManagerController.
     *
     * @return the DefaultAuthManagerController instance.
     */
    public static DefaultAuthManagerController getInstance() {
        if (sInstance == null)
            return new DefaultAuthManagerController();
        return sInstance;
    }

    /**
     * Default private controller constructor.
     */
    private DefaultAuthManagerController() {
        mSessionController = InMemorySessionController.getInstance();
        dbController = LocalSqliteDbControllerFactory
                .getAuthAccessDbController(GlobalObjects.getInstance()
                        .getApplicationContext());
    }

    @Override
    public void requestToken(int _groupId) {
        Log.i(LOG_TAG, "requestToken: requesting new token for " + _groupId);
        if (_groupId < 0)
            return;

        GroupAuth groupAuth = LocalSqliteDbControllerFactory
                .getGroupAuthDbController(GlobalObjects.getInstance()
                        .getApplicationContext()).findById(_groupId);

        if (groupAuth == null)
            return;

        NetworkControllerFactory.getAuthNetworkController().requestAuthToken(new AuthTokenResponse(groupAuth), groupAuth);
    }

    @Override
    public void invalidateToken(int _groupId) {
        if (_groupId < 0)
            return;

        // mSessionController.removeToken(_groupId);
        dbController.updateToken(_groupId, null);
    }

    private class AuthTokenResponse implements ICallbackCompleted<RetrofitAuthToken> {


        private GroupAuth mGroupAuth;

        /**
         * Constructor of AuthTokenResponse.
         *
         * @param _groupAuth the object that holds this information.
         */
        public AuthTokenResponse(GroupAuth _groupAuth) {
            mGroupAuth = _groupAuth;
        }

        @Override
        public void onCompleted(RetrofitAuthToken _next) {
            mSessionController.addOrUpdateToken(mGroupAuth.getGroupId(), _next.token);
            dbController.updateToken(mGroupAuth.getGroupId(), _next.token);
        }

        @Override
        public void onError(Throwable _e) {
            _e.printStackTrace();
            // mSessionController.removeToken(mGroupAuth.getGroupId());
            dbController.updateToken(mGroupAuth.getGroupId(), null);
        }
    }
}
