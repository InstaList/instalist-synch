package org.noorganization.instalistsynch.view.activity;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.local.dba.IGroupMemberDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.impl.DefaultManagerFactory;
import org.noorganization.instalistsynch.events.CreateGroupErrorEvent;
import org.noorganization.instalistsynch.events.CreateGroupNetworkExceptionMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenErrorMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent;
import org.noorganization.instalistsynch.events.HttpResponseCodeErrorMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.GroupExpandableList;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.utils.eSORT_MODE;

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


    /*
    @Override
    public boolean onContextItemSelected(MenuItem _item) {
        ExpandableListView.ExpandableListContextMenuInfo menuInfo = (ExpandableListView.ExpandableListContextMenuInfo) _item.getMenuInfo();

        int groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
        int childPos = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition);

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
    */


    private IGroupMemberDbController mGroupMemberDbController;

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

        mGroupMemberDbController = LocalSqliteDbControllerFactory.getGroupMemberDbController(mContext);
        Cursor authAccessCursor = LocalSqliteDbControllerFactory.getAuthAccessDbController(mContext).getGroupAuthAccessesCursor(eSORT_MODE.ASC);

        SimpleCursorTreeAdapter treeAdapter = new SimpleCursorTreeAdapter(this, authAccessCursor,
                android.R.layout.simple_expandable_list_item_1, android.R.layout.simple_expandable_list_item_1,
                // group
                new String[]{GroupAuthAccess.COLUMN.GROUP_ID}, new int[]{android.R.id.text1},
                //child
                android.R.layout.simple_expandable_list_item_2, android.R.layout.simple_expandable_list_item_2,
                new String[]{GroupMember.COLUMN.NAME, GroupMember.COLUMN.AUTHORIZED}, new int[]{android.R.id.text1, android.R.id.text2}
        ) {
            @Override
            protected Cursor getChildrenCursor(Cursor groupCursor) {
                int groupId = groupCursor.getInt(groupCursor.getColumnIndex(GroupAuthAccess.COLUMN.GROUP_ID));
                return mGroupMemberDbController.getCursorByGroup(groupId);
            }
        };

        mExpandableListView.setAdapter(treeAdapter);

        DefaultManagerFactory.getAuthManagerController().loadAllSessions();


        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String deviceNameForGroup = mDeviceNameInput.getText().toString();
                if (deviceNameForGroup.length() == 0) {
                    mDeviceNameInput.setError("Device name not set");
                    return;
                }
                DefaultManagerFactory.getGroupManagerController().createGroup(deviceNameForGroup);
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
            }
        });

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


    }


    public void onEvent(GroupAccessTokenMessageEvent _msg) {
        mTmpGroupId.setText(_msg.getGroupAccessToken());
    }

    public void onEvent(GroupAccessTokenErrorMessageEvent _msg) {
        Toast.makeText(mContext, _msg.mMsg, Toast.LENGTH_SHORT).show();

    }

    public void onEvent(CreateGroupNetworkExceptionMessageEvent _msg) {
        Toast.makeText(mContext, _msg.mDeviceName + " attempts: " + _msg.mAttempt, Toast.LENGTH_SHORT).show();

    }

    public void onEvent(CreateGroupErrorEvent _msg) {
        Toast.makeText(mContext, _msg.mMessage, Toast.LENGTH_SHORT).show();

    }

    public void onEvent(HttpResponseCodeErrorMessageEvent _msg) {
        switch (_msg.mCode) {
            case 500:
                Toast.makeText(mContext, R.string.internal_server_error, Toast.LENGTH_LONG).show();
                break;
        }
    }


   /* public void populateExpandableListView() {
        IGroupAuthAccessDbController authAccessDbController = LocalSqliteDbControllerFactory.getAuthAccessDbController(mContext);
        mGroupExpandableLists = new ArrayList<>();
        List<GroupAuthAccess> groupAuthAccessList = authAccessDbController.getGroupAuthAccesses();
        for (GroupAuthAccess groupAuthAccess : groupAuthAccessList) {
            mGroupExpandableLists.add(new GroupExpandableList(groupAuthAccess, new ArrayList<GroupMember>()));
        }
        mExpandableListView.setAdapter(new GroupExpandableListAdapter(mGroupExpandableLists, mContext));
    }
    */
}
