package org.noorganization.instalistsynch.activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.adapter.GroupExpandableListAdapter;
import org.noorganization.instalistsynch.controller.local.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.controller.local.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.impl.LocalControllerFactory;
import org.noorganization.instalistsynch.controller.network.IGroupManager;
import org.noorganization.instalistsynch.controller.network.impl.NetworkControllerFactory;
import org.noorganization.instalistsynch.controller.network.impl.V1GroupManager;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent;
import org.noorganization.instalistsynch.events.GroupJoinedMessageEvent;
import org.noorganization.instalistsynch.events.GroupMemberDeleted;
import org.noorganization.instalistsynch.events.GroupMemberListMessageEvent;
import org.noorganization.instalistsynch.events.GroupUpdatedMessageEvent;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.GroupExpandableList;
import org.noorganization.instalistsynch.model.GroupMember;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class SynchOverview extends AppCompatActivity {

    private static String LOG_TAG = SynchOverview.class.getSimpleName();

    private TextView mDebugView;

    /**
     * Device id for the new created group.
     */
    private EditText mDeviceNameInput;
    private EditText mTmpGroupId;
    /**
     * Name of the device for inserted tmp group id.
     */
    private EditText mDeviceName;
    private Button mLoginButton;
    private Button mJoinGroupButton;

    private Context mContext;
    private ArrayAdapter<GroupAuth> mAdapter;

    private ExpandableListView mExpandableListView;
    private List<GroupExpandableList> mGroupExpandableLists;

    @Override
    public boolean onContextItemSelected(MenuItem _item) {
        ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListView.ExpandableListContextMenuInfo) _item.getMenuInfo();

        int groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
        int childPos = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition);

        IGroupManager groupManager = NetworkControllerFactory.getGroupManager();

        // check auth
        // if(groupMember.isAuthorized())
        //   return super.onContextItemSelected(_item);

        switch (_item.getItemId()) {
            case R.id.menu_item_action_authorize:
                GroupMember groupMember = (GroupMember) mExpandableListView.getExpandableListAdapter().getChild(groupPos, childPos);
                GroupAuthAccess groupAuthAccess = (GroupAuthAccess) mExpandableListView.getExpandableListAdapter().getGroup(groupPos);
                Log.i(LOG_TAG, "onContextItemSelected: menu_item_action_authorize");
                groupManager.authorizeGroupMember(groupMember, groupAuthAccess.getToken());
                break;
            case R.id.menu_item_action_remove:
                groupMember = (GroupMember) mExpandableListView.getExpandableListAdapter().getChild(groupPos, childPos);
                groupAuthAccess = (GroupAuthAccess) mExpandableListView.getExpandableListAdapter().getGroup(groupPos);
                Log.i(LOG_TAG, "onContextItemSelected: menu_item_action_remove");
                groupManager.deleteGroupMember(groupMember, groupAuthAccess.getToken());
                break;
            case R.id.menu_item_action_request_group_access_token:
                Log.i(LOG_TAG, "onContextItemSelected: menu_item_action_request_group_access_token");
                groupAuthAccess = (GroupAuthAccess) mExpandableListView.getExpandableListAdapter().getGroup(groupPos);
                String token = groupAuthAccess.getToken();
                groupManager.requestGroupAccessToken(token);
                break;
        }
        return super.onContextItemSelected(_item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.noorganization.instalistsynch.R.layout.activity_synch_overview);

        mContext = this;
        EventBus.getDefault().register(this);

        mExpandableListView = (ExpandableListView) this.findViewById(R.id.testListView);
        // assign textview to debug view
        mDebugView = (TextView) this.findViewById(R.id.text);

        mDeviceNameInput = (EditText) this.findViewById(R.id.login_username_edit_text);
        mTmpGroupId = (EditText) this.findViewById(R.id.tmp_group_id);
        mDeviceName = (EditText) this.findViewById(R.id.device_name);

        mLoginButton = (Button) this.findViewById(R.id.login_submit);
        mJoinGroupButton = (Button) this.findViewById(R.id.join_group);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceNameForGroup = mDeviceNameInput.getText().toString();
                if (deviceNameForGroup.length() == 0) {
                    mDeviceNameInput.setError("Device name not set");
                    return;
                }
                V1GroupManager.getInstance().createGroup(deviceNameForGroup);
            }
        });

        mJoinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean error = false;
                String deviceName = mDeviceName.getText().toString();
                if (deviceName.length() == 0) {
                    mDeviceName.setError("Not set");
                }
                String tmpGroupId = mTmpGroupId.getText().toString();
                if (deviceName.length() == 0) {
                    mTmpGroupId.setError("Not set");
                }
                if (error)
                    return;
                IGroupManager groupManager = NetworkControllerFactory.getGroupManager();
                groupManager.joinGroup(tmpGroupId, deviceName, false);
            }
        });

        /*mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
*/
        mExpandableListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo _menuInfo) {
                ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListView.ExpandableListContextMenuInfo) _menuInfo;

                int groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
                int childPos = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition);

                mExpandableListView.getExpandableListAdapter().getGroup(groupPos);
                MenuInflater inflater = getMenuInflater();

                if (childPos == -1) {
                    // has no child
                    inflater.inflate(R.menu.action_menu_group, menu);
                } else {
                    inflater.inflate(R.menu.action_menu_child, menu);
                }

            }
        });


        //populateListAdapter();

        IGroupAuthDbController groupAuthDbController = LocalControllerFactory.getDefaultAuthController(mContext);
        IGroupManager groupManager = NetworkControllerFactory.getGroupManager();

        List<GroupAuth> groupAuthList = groupAuthDbController.getRegisteredGroups();
        for (GroupAuth groupAuth : groupAuthList) {
            groupManager.requestAuthToken(groupAuth);
        }

        populateExpandableListView();
    }

    public void populateExpandableListView() {
        IGroupAuthAccessDbController authAccessDbController = LocalControllerFactory.getSqliteAuthAccessController(mContext);
        mGroupExpandableLists = new ArrayList<>();
        List<GroupAuthAccess> groupAuthAccessList = authAccessDbController.getGroupAuthAccesses();
        for (GroupAuthAccess groupAuthAccess : groupAuthAccessList) {
            mGroupExpandableLists.add(new GroupExpandableList(groupAuthAccess, new ArrayList<GroupMember>()));
        }
        mExpandableListView.setAdapter(new GroupExpandableListAdapter(mGroupExpandableLists, mContext));
    }

    public void populateListAdapter() {
        List<GroupAuth> groupAuthList = LocalControllerFactory.getDefaultAuthController(this).getRegisteredGroups();
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupAuthList);
        //  mListView.setAdapter(mAdapter);
    }

    public void onEvent(ErrorMessageEvent _msg) {
        Toast.makeText(this, _msg.getErrorMessage(), Toast.LENGTH_LONG).show();
        mDebugView.setText(mDebugView.getText().toString().concat("\n  ").concat(_msg.getErrorMessage()));
        Log.e(LOG_TAG, "onEvent: " + _msg.getErrorMessage());
    }

    public void onEvent(GroupJoinedMessageEvent _msg) {
        if (_msg.mJoined) {
            mDebugView.setText("Join to new group was successful! :)");
            Toast.makeText(this, "Join to group succeeded", Toast.LENGTH_LONG).show();
            populateExpandableListView();
        }
    }

    public void onEvent(TokenMessageEvent _msg) {
        Toast.makeText(this, "Token: " + _msg.getmToken(), Toast.LENGTH_LONG).show();
        mDebugView.setText(mDebugView.getText().toString().concat("\n  ").concat(_msg.getmToken()));
        Log.i(LOG_TAG, "onEvent: " + _msg.getmToken());
        IGroupManager groupManager = NetworkControllerFactory.getGroupManager();
        groupManager.getGroupMembers(_msg.getmToken());
        populateExpandableListView();
        // populateListAdapter();
    }

    public void onEvent(GroupAccessTokenMessageEvent _msg) {
        mDebugView.setText("temporary groupId: " + _msg.getGroupAccessToken());
    }

    public void onEvent(GroupUpdatedMessageEvent _msg) {
        IGroupAuthAccessDbController accessDbController = LocalControllerFactory.getSqliteAuthAccessController(mContext);

        for (GroupAuthAccess groupAuthAccess : accessDbController.getGroupAuthAccesses()) {
            NetworkControllerFactory.getGroupManager().getGroupMembers(groupAuthAccess.getToken());
        }
    }

    public void onEvent(GroupMemberListMessageEvent _msg) {
        for (GroupExpandableList groupExpandableList : mGroupExpandableLists) {
            if (groupExpandableList.getGroupAuthAccess().getDeviceId().equals(_msg.getDeviceId())) {
                groupExpandableList.setGroupMemberList(_msg.getGroupMembers());
            }
        }
        mExpandableListView.setAdapter(new GroupExpandableListAdapter(mGroupExpandableLists, mContext));

    }

    public void onEvent(GroupMemberDeleted _msg) {
        IGroupAuthAccessDbController accessDbController = LocalControllerFactory.getSqliteAuthAccessController(mContext);

        for (GroupAuthAccess groupAuthAccess : accessDbController.getGroupAuthAccesses()) {
            NetworkControllerFactory.getGroupManager().getGroupMembers(groupAuthAccess.getToken());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Refresh");
        menu.add(0, 1, 1, "Clear Database");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                IGroupAuthAccessDbController accessDbController = LocalControllerFactory.getSqliteAuthAccessController(mContext);

                for (GroupAuthAccess groupAuthAccess : accessDbController.getGroupAuthAccesses()) {
                    NetworkControllerFactory.getGroupManager().getGroupMembers(groupAuthAccess.getToken());
                }
                break;
            case 1:
                SynchDbHelper dbHelper = new SynchDbHelper(mContext);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(GroupMember.TABLE_NAME, null, null);
                db.delete(GroupAuth.TABLE_NAME, null, null);
                db.delete(GroupAuthAccess.TABLE_NAME, null, null);
                mAdapter.clear();
                populateExpandableListView();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
