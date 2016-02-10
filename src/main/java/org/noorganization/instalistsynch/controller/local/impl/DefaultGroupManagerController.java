package org.noorganization.instalistsynch.controller.local.impl;

import android.content.Context;
import android.util.Log;

import org.noorganization.instalistsynch.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.local.IGroupManagerController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupMemberDbController;
import org.noorganization.instalistsynch.controller.local.dba.ITempGroupAccessTokenDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.network.IGroupNetworkController;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.impl.NetworkControllerFactory;
import org.noorganization.instalistsynch.events.CreateGroupErrorEvent;
import org.noorganization.instalistsynch.events.CreateGroupNetworkExceptionMessageEvent;
import org.noorganization.instalistsynch.events.DeletedMemberMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenErrorMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent;
import org.noorganization.instalistsynch.events.GroupMemberListMessageEvent;
import org.noorganization.instalistsynch.events.GroupMemberNotExistingMessageEvent;
import org.noorganization.instalistsynch.events.LocalGroupExistsEvent;
import org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent;
import org.noorganization.instalistsynch.model.AccessRight;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.TempGroupAccessToken;
import org.noorganization.instalistsynch.model.network.response.GroupAccessKey;
import org.noorganization.instalistsynch.model.network.response.GroupMemberRetrofit;
import org.noorganization.instalistsynch.model.network.response.GroupResponse;
import org.noorganization.instalistsynch.model.network.response.RegisterDeviceResponse;
import org.noorganization.instalistsynch.model.observable.GroupMemberAuthorized;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * The DefaultGroupManagerController the default implementation of {@link IGroupManagerController}
 * Created by tinos_000 on 08.02.2016.
 */
public class DefaultGroupManagerController implements IGroupManagerController {
    private static final String LOG_TAG = DefaultGroupManagerController.class.getSimpleName();

    private static DefaultGroupManagerController sInstance;
    private IGroupAuthDbController mGroupAuthDbController;
    private IGroupMemberDbController mGroupMemberDbController;
    private ITempGroupAccessTokenDbController mTempGroupAccessTokenDbController;
    private IGroupNetworkController mGroupNetworkController;
    private ISessionController mSessionController;
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
        mGroupMemberDbController = LocalSqliteDbControllerFactory.getGroupMemberDbController(mContext);
        mGroupNetworkController = NetworkControllerFactory.getGroupController();
        mSessionController = InMemorySessionController.getInstance();
    }

    @Override
    public void createGroup(String _deviceName) {

        if (mGroupAuthDbController.hasOwnLocalGroup()) {
            EventBus.getDefault().post(new LocalGroupExistsEvent());
            return;
        }

        TempGroupAccessToken accessToken = mTempGroupAccessTokenDbController.getLocalAccessToken();
        // if there is an access token available, we know that the device has not joined the group yet.
        if (accessToken != null) {
            int groupId = accessToken.getGroupId();
           // requestGroupAccessToken(groupId);
            //joinGroup(accessToken.getGroupAccessToken(), _deviceName, true, groupId);
         //   return;
        }

        mGroupNetworkController.createGroup(new CreateGroupResponse(_deviceName));
    }

    @Override
    public void joinGroup(String _groupAccessKey, String _deviceName, boolean _isLocal, int _groupId) {
        Log.i(LOG_TAG, "joinGroup: " + _groupAccessKey);
        SecureRandom secureRandom = GlobalObjects.getInstance().getSecureRandom();
        String secret = new BigInteger(196, secureRandom).toString(32);
        GroupAuth group = new GroupAuth();
        group.setGroupId(_groupId);
        group.setDeviceName(_deviceName);
        group.setSecret(secret);
        mGroupNetworkController.joinGroup(new JoinGroupResponse(group, _isLocal), _groupAccessKey, _deviceName, _groupId, secret);
    }

    @Override
    public void deleteMemberOfGroup(int _groupId, int _deviceId) {
        String authToken = mSessionController.getToken(_groupId);
        // lazy loading, do not check if null, because an event is triggered when token is not valid.
        mGroupNetworkController.deleteMemberOfGroup(new DeleteMemberResponse(_groupId, _deviceId), authToken, _groupId, _deviceId);
    }

    @Override
    public void requestGroupAccessToken(int _groupId) {
        String authToken = mSessionController.getToken(_groupId);
        mGroupNetworkController.requestGroupAccessToken(new GroupAccessKeyResponse(_groupId), _groupId, authToken);
    }

    @Override
    public void getGroupMembers(int _groupId) {
        String authToken = mSessionController.getToken(_groupId);
        mGroupNetworkController.getGroupMembers(new GetGroupMemberResponse(_groupId), _groupId, authToken);
    }

    @Override
    public void authorizeGroupMember(GroupMember _groupMember, int _groupId) {
        String authToken = mSessionController.getToken(_groupId);
        mGroupNetworkController.authorizeGroupMember(new GroupMemberAuthorizeResponse(_groupId, _groupMember.getDeviceId()), _groupMember, authToken);
    }

    // -----------------------------------------------------------------------------------------

    /**
     * Response for createGroup request.
     */
    private class CreateGroupResponse implements ICallbackCompleted<GroupResponse> {

        private String mDeviceName;

        public CreateGroupResponse(String _deviceName) {
            mDeviceName = _deviceName;
        }

        @Override
        public void onCompleted(GroupResponse _next) {
            mTempGroupAccessTokenDbController.insertAccessToken(_next.id, _next.accesskey, true);
            joinGroup(_next.accesskey, mDeviceName, true, _next.id);
        }

        @Override
        public void onError(Throwable _e) {
            if (_e instanceof IOException) {
                EventBus.getDefault().post(new CreateGroupNetworkExceptionMessageEvent(mDeviceName, 0));
            } else {
                EventBus.getDefault().post(new CreateGroupErrorEvent(_e.getLocalizedMessage()));
            }
        }
    }

    // -----------------------------------------------------------------------------------------

    /**
     * Response for joinGroup request.
     */
    private class JoinGroupResponse implements ICallbackCompleted<RegisterDeviceResponse> {

        private GroupAuth mGroup;
        private boolean mIsLocal;

        public JoinGroupResponse(GroupAuth _group, boolean _isLocal) {
            mGroup = _group;
            mIsLocal = _isLocal;
        }

        @Override
        public void onCompleted(RegisterDeviceResponse _next) {
            GroupAuth groupAuth = new GroupAuth(mGroup.getGroupId(), _next.deviceid, mGroup.getSecret(), mGroup.getDeviceName(), mIsLocal);
            mGroupAuthDbController.insertRegisteredGroup(groupAuth);
            mTempGroupAccessTokenDbController.deleteAccessToken(mGroup.getGroupId());

            // send this event to trigger authToken fetch!
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(groupAuth.getGroupId(), groupAuth.getDeviceId()));
        }

        @Override
        public void onError(Throwable _e) {
            String m = _e.getLocalizedMessage();
            return;
            // some not transient error must happened!
        }
    }

    // -----------------------------------------------------------------------------------------

    /**
     * Response of a delete member request.
     */
    private class DeleteMemberResponse implements IAuthorizedCallbackCompleted<Void> {

        private int mGroupId;
        private int mDeviceId;

        public DeleteMemberResponse(int _groupId, int _deviceId) {
            mGroupId = _groupId;
            mDeviceId = _deviceId;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, -1));
        }

        @Override
        public void onCompleted(Void _next) {
            mGroupMemberDbController.delete(mGroupId, mDeviceId);
            Log.i(LOG_TAG, "onCompleted: Delete member in database");
            EventBus.getDefault().post(new DeletedMemberMessageEvent(mGroupId, mDeviceId));
        }

        @Override
        public void onError(Throwable _e) {
            // some not transient error must happened!
        }
    }

    /**
     * Response for getGroupAccessKey request.
     */
    private class GroupAccessKeyResponse implements IAuthorizedCallbackCompleted<GroupAccessKey> {

        private int mGroupId;

        public GroupAccessKeyResponse(int _groupId) {
            mGroupId = _groupId;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, -1));
        }

        @Override
        public void onCompleted(GroupAccessKey _next) {
            EventBus.getDefault().post(new GroupAccessTokenMessageEvent(mGroupId, _next.groupid));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new GroupAccessTokenErrorMessageEvent(_e.getLocalizedMessage()));
        }
    }

    /**
     * Response for get groupMembers request.
     */
    private class GetGroupMemberResponse implements IAuthorizedCallbackCompleted<List<GroupMemberRetrofit>> {

        private int mGroupId;

        public GetGroupMemberResponse(int groupId) {
            mGroupId = groupId;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, -1));
        }

        @Override
        public void onCompleted(List<GroupMemberRetrofit> _next) {
            List<GroupMember> groupMemberList = new ArrayList<>(_next.size());
            for (GroupMemberRetrofit groupMemberRetrofit : _next) {
                GroupMember groupMember = mGroupMemberDbController.getById(mGroupId, groupMemberRetrofit.id);
                AccessRight accessRight = new AccessRight(groupMemberRetrofit.authorized, groupMemberRetrofit.authorized);
                GroupMember newMember = new GroupMember(mGroupId, groupMemberRetrofit.id, groupMemberRetrofit.name, accessRight);
                groupMemberList.add(newMember);
                if (groupMember == null) {
                    // insert
                    mGroupMemberDbController.insert(newMember);
                    Log.i(LOG_TAG, "onCompleted: insert new member: " + groupMemberRetrofit.name);
                } else if (!groupMember.equals(newMember)) {
                    //update
                    mGroupMemberDbController.update(newMember);
                    Log.i(LOG_TAG, "onCompleted: update new member: " + groupMemberRetrofit.name);
                }
            }

            EventBus.getDefault().post(new GroupMemberListMessageEvent(groupMemberList, mGroupId));
        }

        @Override
        public void onError(Throwable _e) {
            // updating has had an error.
        }
    }

    /**
     * Response for authorize GroupMember request.
     */
    private class GroupMemberAuthorizeResponse implements IAuthorizedCallbackCompleted<Void> {

        private int mGroupId;
        private int mDeviceId;

        public GroupMemberAuthorizeResponse(int _groupId, int _deviceId) {
            mGroupId = _groupId;
            mDeviceId = _deviceId;
        }

        @Override
        public void onUnauthorized(int _groupId) {
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, -1));
        }

        @Override
        public void onCompleted(Void _next) {
            GroupMember groupMember = mGroupMemberDbController.getById(mGroupId, mDeviceId);
            if (groupMember == null) {
                EventBus.getDefault().post(new GroupMemberNotExistingMessageEvent());
                return;
            }

            groupMember.setAccessRights(new AccessRight(true, true));
            mGroupMemberDbController.update(groupMember);
            EventBus.getDefault().post(new GroupMemberAuthorized(groupMember, true));
        }

        @Override
        public void onError(Throwable _e) {
            // authorizing has had an error.
        }
    }
}
