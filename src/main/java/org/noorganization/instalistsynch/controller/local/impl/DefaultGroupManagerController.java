package org.noorganization.instalistsynch.controller.local.impl;

import android.content.Context;
import android.util.Log;

import org.noorganization.instalistsynch.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.local.IGroupManagerController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.ITempGroupAccessTokenDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.network.IGroupNetworkController;
import org.noorganization.instalistsynch.controller.network.impl.NetworkControllerFactory;
import org.noorganization.instalistsynch.events.CreateGroupErrorEvent;
import org.noorganization.instalistsynch.events.CreateGroupNetworkExceptionMessageEvent;
import org.noorganization.instalistsynch.events.LocalGroupExistsEvent;
import org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent;
import org.noorganization.instalistsynch.model.Group;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.network.response.GroupResponse;
import org.noorganization.instalistsynch.model.network.response.RegisterDeviceResponse;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import de.greenrobot.event.EventBus;
import retrofit2.HttpException;

/**
 * The DefaultGroupManagerController the default implementation of {@link IGroupManagerController}
 * Created by tinos_000 on 08.02.2016.
 */
public class DefaultGroupManagerController implements IGroupManagerController {
    private static final String LOG_TAG = DefaultGroupManagerController.class.getSimpleName();

    private static DefaultGroupManagerController sInstance;
    private IGroupAuthDbController mGroupAuthDbController;
    private ITempGroupAccessTokenDbController mTempGroupAccessTokenDbController;
    private IGroupNetworkController mGroupNetworkController;
    private Context mContext;

    /**
     * Get the DefaultGroupManagerController instance.
     *
     * @return DefaultGroupManagerController instance.
     */
    public static DefaultGroupManagerController getInstance() {
        if (sInstance == null)
            sInstance = new DefaultGroupManagerController();
        return sInstance;
    }

    private DefaultGroupManagerController() {
        mContext = GlobalObjects.getInstance().getApplicationContext();
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(mContext);
        mTempGroupAccessTokenDbController = LocalSqliteDbControllerFactory.getTempGroupAccessTokenDbController(mContext);
        mGroupNetworkController = NetworkControllerFactory.getGroupController();
    }

    @Override
    public void createGroup(String _deviceName) {

        if (mGroupAuthDbController.hasOwnLocalGroup()) {
            EventBus.getDefault().post(new LocalGroupExistsEvent());
            return;
        }

        mGroupNetworkController.createGroup(new CreateGroupResponse(_deviceName));
    }

    @Override
    public void joinGroup(String _groupAccessKey, String _deviceName, boolean _isLocal, int _groupId) {
        Log.i(LOG_TAG, "joinGroup: " + _groupAccessKey);
        SecureRandom secureRandom = GlobalObjects.getInstance().getSecureRandom();
        String secret = new BigInteger(196, secureRandom).toString(32);
        Group group = new Group(_groupId, secret, _deviceName);

        mGroupNetworkController.joinGroup(new JoinGroupResponse(group, _isLocal), _groupAccessKey, _deviceName, _groupId, secret);
    }

    @Override
    public void leaveGroup(GroupAuth _groupAuth) {

    }

    @Override
    public void requestGroupAccessToken(String _authToken) {

    }

    @Override
    public void getGroupMembers(String _authToken) {

    }

    @Override
    public void deleteGroupMember(GroupMember _groupMember, String _token) {

    }

    @Override
    public void authorizeGroupMember(GroupMember _groupMember, String _token) {

    }

    // -----------------------------------------------------------------------------------------

    private class CreateGroupResponse implements ICallbackCompleted<GroupResponse> {

        private String mDeviceName;

        public CreateGroupResponse(String _deviceName) {
            mDeviceName = _deviceName;
        }

        @Override
        public void onCompleted(GroupResponse _next) {
            joinGroup(_next.accesskey, mDeviceName, true, _next.id);
        }

        @Override
        public void onError(Throwable _e) {
            if (_e instanceof HttpException) {
                HttpException e = (HttpException) _e;
                EventBus.getDefault().post(new CreateGroupErrorEvent(e.message()));
            } else if (_e instanceof IOException) {
                EventBus.getDefault().post(new CreateGroupNetworkExceptionMessageEvent(mDeviceName, 0));
            } else {
                EventBus.getDefault().post(new CreateGroupErrorEvent(_e.getLocalizedMessage()));
            }
        }
    }

    // -----------------------------------------------------------------------------------------


    private class JoinGroupResponse implements ICallbackCompleted<RegisterDeviceResponse> {

        private Group mGroup;
        private boolean mIsLocal;

        public JoinGroupResponse(Group _group, boolean _isLocal) {
            mGroup = _group;
            mIsLocal = _isLocal;
        }

        @Override
        public void onCompleted(RegisterDeviceResponse _next) {
            GroupAuth groupAuth = new GroupAuth(mGroup.groupid, _next.deviceid, mGroup.secret, mGroup.name, mIsLocal);
            mGroupAuthDbController.insertRegisteredGroup(groupAuth);
            // send this event to trigger authToken fetch!
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(groupAuth.getGroupId(), groupAuth.getDeviceId()));
        }

        @Override
        public void onError(Throwable _e) {
            // TODO what happens if the connection is lost? Requery ? or simply notify user for retry?
        }
    }

}
