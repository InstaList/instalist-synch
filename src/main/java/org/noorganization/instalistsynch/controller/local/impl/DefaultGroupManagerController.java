/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.local.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import org.noorganization.instalist.comm.message.DeviceInfo;
import org.noorganization.instalist.comm.message.GroupInfo;
import org.noorganization.instalistsynch.controller.callback.IAuthorizedCallbackCompleted;
import org.noorganization.instalistsynch.controller.callback.ICallbackCompleted;
import org.noorganization.instalistsynch.controller.local.IGroupManagerController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupMemberDbController;
import org.noorganization.instalistsynch.controller.local.dba.ITempGroupAccessTokenDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.network.IGroupNetworkController;
import org.noorganization.instalistsynch.controller.network.ISessionController;
import org.noorganization.instalistsynch.controller.network.impl.InMemorySessionController;
import org.noorganization.instalistsynch.controller.network.AuthNetworkControllerFactory;
import org.noorganization.instalistsynch.events.CreateGroupErrorEvent;
import org.noorganization.instalistsynch.events.CreateGroupNetworkExceptionMessageEvent;
import org.noorganization.instalistsynch.events.DeletedMemberMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenErrorMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent;
import org.noorganization.instalistsynch.events.GroupJoinedMessageEvent;
import org.noorganization.instalistsynch.events.GroupMemberNotExistingMessageEvent;
import org.noorganization.instalistsynch.events.GroupMemberUpdateMessageEvent;
import org.noorganization.instalistsynch.events.LocalGroupExistsEvent;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.events.UnauthorizedErrorMessageEvent;
import org.noorganization.instalistsynch.model.AccessRight;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.TempGroupAccessToken;
import org.noorganization.instalistsynch.events.GroupMemberAuthorizedMessageEvent;
import org.noorganization.instalistsynch.utils.Constants;
import org.noorganization.instalistsynch.utils.GlobalObjects;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * The DefaultGroupManagerController the default implementation of {@link IGroupManagerController}
 * Created by tinos_000 on 08.02.2016.
 */
public class DefaultGroupManagerController implements IGroupManagerController {
    private static final String LOG_TAG = DefaultGroupManagerController.class.getSimpleName();

    private static DefaultGroupManagerController     sInstance;
    private        IGroupAuthDbController            mGroupAuthDbController;
    private        IGroupMemberDbController          mGroupMemberDbController;
    private        ITempGroupAccessTokenDbController mTempGroupAccessTokenDbController;
    private        IGroupNetworkController           mGroupNetworkController;
    private        IGroupAuthAccessDbController      mGroupAuthAccessDbController;
    private        ISessionController                mSessionController;
    private        Context                           mContext;

    /**
     * The request type that is buffered and waits for a token.
     */
    enum eRequestType {
        GROUP_ACCESS_TOKEN,
        DELETE_MEMBER,
        GET_MEMBERS,
        AUTHORIZE_MEMBER,
        ALL,;
    }

    /**
     * All cached tasks that should be executed.
     */
    private Map<Integer, Map<eRequestType, Object>> mCachedTaskObjects;

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
        mTempGroupAccessTokenDbController =
                LocalSqliteDbControllerFactory.getTempGroupAccessTokenDbController(mContext);
        mGroupMemberDbController =
                LocalSqliteDbControllerFactory.getGroupMemberDbController(mContext);
        mGroupAuthAccessDbController =
                LocalSqliteDbControllerFactory.getAuthAccessDbController(mContext);
        mGroupNetworkController = AuthNetworkControllerFactory.getGroupController();
        mSessionController = InMemorySessionController.getInstance();
        mCachedTaskObjects = new HashMap();
        EventBus.getDefault().register(this);
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
            joinGroup(accessToken.getGroupAccessToken(), _deviceName, true, groupId);
            return;
        }

        mGroupNetworkController.createGroup(new CreateGroupResponse(_deviceName));
    }

    @Override
    public void joinGroup(String _groupAccessKey, String _deviceName, boolean _isLocal,
            int _groupId) {
        Log.i(LOG_TAG, "joinGroup: " + _groupAccessKey);
        SecureRandom secureRandom = GlobalObjects.getInstance().getSecureRandom();
        String       secret       = new BigInteger(196, secureRandom).toString(32);
        GroupAuth    group        = new GroupAuth();
        group.setGroupId(_groupId);
        group.setDeviceName(_deviceName);
        group.setSecret(secret);
        mGroupNetworkController.joinGroup(new JoinGroupResponse(group, _isLocal),
                _groupAccessKey,
                _deviceName,
                _groupId,
                secret);
    }

    @Override
    public void deleteMemberOfGroup(int _groupId, int _deviceId) {
        String authToken = mSessionController.getToken(_groupId);
        DeleteMemberResponse deleteGroupMember = new DeleteMemberResponse(_groupId, _deviceId);

        if (authToken != null) {
            mGroupNetworkController.deleteMemberOfGroup(deleteGroupMember, authToken, _groupId, _deviceId);
        } else {
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, _deviceId));
            queueAuthorizedQueue(_groupId, eRequestType.DELETE_MEMBER, deleteGroupMember);
        }

    }

    @Override
    public void requestGroupAccessToken(int _groupId) {
        String authToken = mSessionController.getToken(_groupId);
        GroupAccessKeyResponse groupAccessKeyResponse = new GroupAccessKeyResponse(_groupId);
        if (authToken != null) {
            mGroupNetworkController.requestGroupAccessToken(groupAccessKeyResponse, _groupId, authToken);
        } else {
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, -1));
            queueAuthorizedQueue(_groupId, eRequestType.GROUP_ACCESS_TOKEN, groupAccessKeyResponse);
        }
    }

    @Override
    public void getGroupMembers(int _groupId) {
        String authToken = mSessionController.getToken(_groupId);

        GetGroupMemberResponse response = new GetGroupMemberResponse(_groupId);
        if (authToken != null) {
            mGroupNetworkController.getGroupMembers(response, _groupId, authToken);
        } else {
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, -1));
            queueAuthorizedQueue(_groupId, eRequestType.GET_MEMBERS, response);
        }
    }


    @Override
    public void authorizeGroupMember(int _groupId, int _deviceId) {
        String authToken = mSessionController.getToken(_groupId);
        GroupMember groupMember = mGroupMemberDbController.getById(_groupId, _deviceId);
        GroupMemberAuthorizeResponse response = new GroupMemberAuthorizeResponse(_groupId, _deviceId);

        if (authToken != null) {
            mGroupNetworkController.authorizeGroupMember(response, groupMember, authToken);

        } else {
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(_groupId, -1));
            queueAuthorizedQueue(_groupId, eRequestType.AUTHORIZE_MEMBER, response);
        }
    }

    @Override
    public void refreshGroupMember() {
        for (GroupAuth groupAuth : mGroupAuthDbController.getRegisteredGroups()) {
            getGroupMembers(groupAuth.getGroupId());
        }
    }

    @SuppressLint("Eventbus")
    public void onEvent(TokenMessageEvent _msg) {
        Map<eRequestType, Object> map = mCachedTaskObjects.get(_msg.getGroupId());
        if (map == null || map.size() == 0)
            return;

        mCachedTaskObjects.remove(_msg.getGroupId());
        for (eRequestType requestType : map.keySet()) {
            Object obj = map.get(requestType);
            switch (requestType) {
                case GROUP_ACCESS_TOKEN:
                    GroupAccessKeyResponse accessKeyResponse = (GroupAccessKeyResponse) obj;
                    requestGroupAccessToken(accessKeyResponse.mGroupId);
                    break;
                case DELETE_MEMBER:
                    DeleteMemberResponse deleteMemberResponse = (DeleteMemberResponse) obj;
                    deleteMemberOfGroup(deleteMemberResponse.mGroupId, deleteMemberResponse.mDeviceId);
                    break;
                case GET_MEMBERS:
                    GetGroupMemberResponse groupMemberResponse = (GetGroupMemberResponse) obj;
                    getGroupMembers(groupMemberResponse.mGroupId);
                    break;
                case AUTHORIZE_MEMBER:
                    GroupMemberAuthorizeResponse groupMemberAuthorizeResponse = (GroupMemberAuthorizeResponse) obj;
                    authorizeGroupMember(groupMemberAuthorizeResponse.mGroupId, groupMemberAuthorizeResponse.mDeviceId);
                    break;
                case ALL:
                    break;
            }
        }
    }


    /**
     * Queue an authorized action if there is no auth token.
     *
     * @param _groupId       the id of the group.
     * @param _requestType   the type of the request.
     * @param _requestObject the requested object.
     */
    private void queueAuthorizedQueue(int _groupId, eRequestType _requestType, Object _requestObject) {
        if (mCachedTaskObjects.get(_groupId) == null) {
            mCachedTaskObjects.put(_groupId, new HashMap<eRequestType, Object>(eRequestType.ALL.ordinal()));
        }
        Map<eRequestType, Object> map = mCachedTaskObjects.get(_groupId);
        map.put(_requestType, _requestObject);
        mCachedTaskObjects.put(_groupId, map);
    }


    // -----------------------------------------------------------------------------------------

    /**
     * Response for createGroup request.
     */
    private class CreateGroupResponse implements ICallbackCompleted<GroupInfo> {

        private String mDeviceName;

        public CreateGroupResponse(String _deviceName) {
            mDeviceName = _deviceName;
        }

        @Override
        public void onCompleted(GroupInfo _next) {
            mTempGroupAccessTokenDbController.insertAccessToken(_next.getId(), _next.getReadableId(), true);
            joinGroup(_next.getReadableId(), mDeviceName, true, _next.getId());
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
    private class JoinGroupResponse implements ICallbackCompleted<DeviceInfo> {

        private GroupAuth mGroup;
        private boolean mIsLocal;

        public JoinGroupResponse(GroupAuth _group, boolean _isLocal) {
            mGroup = _group;
            mIsLocal = _isLocal;
        }

        @Override
        public void onCompleted(DeviceInfo _next) {
            mTempGroupAccessTokenDbController.deleteAccessToken(mGroup.getGroupId());

            GroupAuth groupAuth = new GroupAuth(mGroup.getGroupId(), _next.getId(), mGroup.getSecret(), mGroup.getDeviceName(), mIsLocal);

            if (!mGroupAuthDbController.insertRegisteredGroup(groupAuth)) {
                onError(new Throwable("Insertion into authDb failed!"));
                return;
            }

            GroupAccess groupAccess = new GroupAccess(mGroup.getGroupId(), null);
            groupAccess.setLastUpdateFromServer(new Date(Constants.INITIAL_DATE));
            groupAccess.setLastUpdateFromClient(new Date(Constants.INITIAL_DATE));
            groupAccess.setLastTokenRequest(new Date(Constants.INITIAL_DATE));
            groupAccess.setSynchronize(true);
            groupAccess.setInterrupted(false);
            if (IGroupAuthAccessDbController.INSERTION_CODE.CORRECT != mGroupAuthAccessDbController.insert(
                    groupAccess)) {
                // rollback needed
                mGroupAuthDbController.removeRegisteredGroup(mGroup.getGroupId());
                onError(new Throwable("Cannot insert into groupAccess " + groupAccess.toString()));
                return;
            }

            EventBus.getDefault().post(new GroupJoinedMessageEvent());
            // send this event to trigger authToken fetch!
            EventBus.getDefault().post(new UnauthorizedErrorMessageEvent(groupAuth.getGroupId(), groupAuth.getDeviceId()));

            getGroupMembers(mGroup.getGroupId());
        }

        @Override
        public void onError(Throwable _e) {
            Log.e(LOG_TAG, "onError: ", _e);
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
    private class GroupAccessKeyResponse implements IAuthorizedCallbackCompleted<GroupInfo> {

        private int mGroupId;

        public GroupAccessKeyResponse(int _groupId) {
            mGroupId = _groupId;
        }

        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(GroupInfo _next) {
            EventBus.getDefault().post(new GroupAccessTokenMessageEvent(mGroupId, _next.getReadableId()));
        }

        @Override
        public void onError(Throwable _e) {
            EventBus.getDefault().post(new GroupAccessTokenErrorMessageEvent(_e.getLocalizedMessage()));
        }
    }

    /**
     * Response for get groupMembers request.
     */
    private class GetGroupMemberResponse implements IAuthorizedCallbackCompleted<List<DeviceInfo>> {

        private int mGroupId;

        public GetGroupMemberResponse(int groupId) {
            mGroupId = groupId;
        }

        @Override
        public void onUnauthorized(int _groupId) {
        }

        @Override
        public void onCompleted(List<DeviceInfo> _next) {
            List<GroupMember> groupMemberList = new ArrayList<>(_next.size());
            for (DeviceInfo groupMemberRetrofit : _next) {
                GroupMember groupMember = mGroupMemberDbController.getById(mGroupId, groupMemberRetrofit.getId());
                AccessRight accessRight = new AccessRight(groupMemberRetrofit.getAuthorized(), groupMemberRetrofit.getAuthorized());
                GroupMember newMember = new GroupMember(mGroupId, groupMemberRetrofit.getId(), groupMemberRetrofit.getName(), accessRight);
                groupMemberList.add(newMember);
                if (groupMember == null) {
                    // insert
                    mGroupMemberDbController.insert(newMember);
                    Log.i(LOG_TAG, "onCompleted: insert new member: " + groupMemberRetrofit.getName());
                } else if (!groupMember.equals(newMember)) {
                    //update
                    mGroupMemberDbController.update(newMember);
                    Log.i(LOG_TAG, "onCompleted: update new member: " + groupMemberRetrofit.getName());
                }
            }
            EventBus.getDefault().post(new GroupMemberUpdateMessageEvent(mGroupId));
            // EventBus.getDefault().post(new GroupMemberListMessageEvent(groupMemberList, mGroupId));
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
            EventBus.getDefault().post(new GroupMemberAuthorizedMessageEvent(groupMember, true));
        }

        @Override
        public void onError(Throwable _e) {
            // authorizing has had an error.
        }
    }
}
