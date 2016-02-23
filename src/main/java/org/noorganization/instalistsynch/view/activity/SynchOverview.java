package org.noorganization.instalistsynch.view.activity;

import android.content.Context;
import android.database.Cursor;
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
import android.widget.SimpleCursorTreeAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.local.IGroupManagerController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.IGroupMemberDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.controller.local.impl.DefaultManagerFactory;
import org.noorganization.instalistsynch.controller.synch.SynchManager;
import org.noorganization.instalistsynch.events.CreateGroupErrorEvent;
import org.noorganization.instalistsynch.events.CreateGroupNetworkExceptionMessageEvent;
import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenErrorMessageEvent;
import org.noorganization.instalistsynch.events.GroupAccessTokenMessageEvent;
import org.noorganization.instalistsynch.events.GroupJoinedMessageEvent;
import org.noorganization.instalistsynch.events.GroupMemberUpdateMessageEvent;
import org.noorganization.instalistsynch.events.HttpResponseCodeErrorMessageEvent;
import org.noorganization.instalistsynch.events.LocalGroupExistsEvent;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.GroupExpandableList;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.eSortMode;

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
    private Button   mLoginButton;
    private Button   mJoinGroupButton;
    private Button   mSynchButton;

    private Context                 mContext;
    private ArrayAdapter<GroupAuth> mAdapter;

    private ExpandableListView        mExpandableListView;
    private List<GroupExpandableList> mGroupExpandableLists;

    private SimpleCursorTreeAdapter mSimpleCursorTreeAdapter;
    private IGroupManagerController mGroupManagerController;

    private boolean mTempSynchFlag;

    @Override
    public boolean onContextItemSelected(MenuItem _item) {
        ExpandableListView.ExpandableListContextMenuInfo menuInfo =
                (ExpandableListView.ExpandableListContextMenuInfo) _item.getMenuInfo();

        int groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
        int childPos = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition);

        switch (_item.getItemId()) {
            case R.id.menu_item_action_authorize:
                Cursor groupMember = (Cursor) mExpandableListView.getExpandableListAdapter()
                        .getChild(groupPos, childPos);
                Log.i(LOG_TAG, "onContextItemSelected: menu_item_action_authorize");
                mGroupManagerController.authorizeGroupMember(groupMember
                                .getInt(groupMember.getColumnIndex(GroupMember.COLUMN.GROUP_ID)),
                        groupMember
                                .getInt(groupMember.getColumnIndex(GroupMember.COLUMN.DEVICE_ID)));
                break;
            case R.id.menu_item_action_remove:
                groupMember = (Cursor) mExpandableListView.getExpandableListAdapter()
                        .getChild(groupPos, childPos);
                Log.i(LOG_TAG, "onContextItemSelected: menu_item_action_remove");
                mGroupManagerController.deleteMemberOfGroup(groupMember
                                .getInt(groupMember.getColumnIndex(GroupMember.COLUMN.GROUP_ID)),
                        groupMember
                                .getInt(groupMember.getColumnIndex(GroupMember.COLUMN.DEVICE_ID)));
                break;
            case R.id.menu_item_action_request_group_access_token:
                groupMember =
                        (Cursor) mExpandableListView.getExpandableListAdapter().getGroup(groupPos);
                Log.i(LOG_TAG,
                        "onContextItemSelected: menu_item_action_request_group_access_token");
                mGroupManagerController.requestGroupAccessToken(groupMember
                        .getInt(groupMember.getColumnIndex(GroupAuthAccess.COLUMN.GROUP_ID)));
                break;
        }
        return super.onContextItemSelected(_item);
    }


    private IGroupMemberDbController mGroupMemberDbController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTempSynchFlag = false;
        setContentView(org.noorganization.instalistsynch.R.layout.activity_synch_overview);

        DefaultManagerFactory.getAuthManagerController().loadAllSessions();

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
        mSynchButton = (Button) this.findViewById(R.id.synchButton);

        mGroupManagerController = DefaultManagerFactory.getGroupManagerController();
        mGroupMemberDbController =
                LocalSqliteDbControllerFactory.getGroupMemberDbController(mContext);
        Cursor authAccessCursor = LocalSqliteDbControllerFactory.getAuthAccessDbController(mContext)
                .getGroupAuthAccessesCursor(eSortMode.ASC);

        mSimpleCursorTreeAdapter = new SimpleCursorTreeAdapter(this,
                authAccessCursor,
                android.R.layout.simple_expandable_list_item_1,
                android.R.layout.simple_expandable_list_item_1,
                // group
                new String[]{GroupAuthAccess.COLUMN.GROUP_ID},
                new int[]{android.R.id.text1},
                //child
                android.R.layout.simple_expandable_list_item_2,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{GroupMember.COLUMN.NAME, GroupMember.COLUMN.AUTHORIZED},
                new int[]{android.R.id.text1, android.R.id.text2}
        ) {
            @Override
            protected Cursor getChildrenCursor(Cursor groupCursor) {
                int groupId = groupCursor
                        .getInt(groupCursor.getColumnIndex(GroupAuthAccess.COLUMN.GROUP_ID));
                return mGroupMemberDbController.getCursorByGroup(groupId);
            }
        };

        mExpandableListView.setAdapter(mSimpleCursorTreeAdapter);

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
                boolean error      = false;
                String  deviceName = mDeviceName.getText().toString();
                if (deviceName.length() == 0) {
                    mDeviceName.setError("Not set");
                }
                String tmpGroupId = mTmpGroupId.getText().toString();
                if (deviceName.length() == 0) {
                    mTmpGroupId.setError("Not set");
                }
                if (error) {
                    return;
                }
            }
        });

        mSynchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IGroupAuthDbController groupAuthDbController =
                        LocalSqliteDbControllerFactory.getGroupAuthDbController(mContext);
                List<GroupAuth> groups =
                        groupAuthDbController.getRegisteredGroups();
                SynchManager synchManager = new SynchManager();
                for (GroupAuth group : groups) {
                    if (group.isLocal() && !mTempSynchFlag) {
                        synchManager.index(group.getGroupId());
                        mTempSynchFlag = true;
                    }
                    synchManager.synchronize(group.getGroupId());
                }
            }
        });

        mExpandableListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                    ContextMenu.ContextMenuInfo _menuInfo) {
                ExpandableListView.ExpandableListContextMenuInfo menuInfo =
                        (ExpandableListView.ExpandableListContextMenuInfo) _menuInfo;

                int groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition);
                int childPos = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition);

                mExpandableListView.getExpandableListAdapter().getGroup(groupPos);
                MenuInflater inflater = getMenuInflater();

                if (childPos == -1) {
                    // has no child
                    inflater.inflate(R.menu.action_menu_group, menu);
                } else {
                    Cursor cursor = mSimpleCursorTreeAdapter.getChild(groupPos, childPos);

                    if (cursor.getCount() > 1) {
                        boolean authorized =
                                cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.AUTHORIZED))
                                        == 1;
                        inflater.inflate(R.menu.action_menu_child, menu);
                        if (authorized) {
                            menu.removeItem(R.id.menu_item_action_authorize);
                        }
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_refresh:
                mGroupManagerController.refreshGroupMember();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Updates the child cursor, so that the data is on a current state.
     *
     * @param _msg the message with the info of the updated group.
     */
    public void onEvent(GroupMemberUpdateMessageEvent _msg) {
        Cursor cursor = LocalSqliteDbControllerFactory.getGroupAuthDbController(mContext)
                .getRegisteredGroupsCursor();
        mSimpleCursorTreeAdapter.changeCursor(cursor);
    }

    public void onEvent(GroupJoinedMessageEvent _msg) {
        Cursor cursor = LocalSqliteDbControllerFactory.getAuthAccessDbController(mContext)
                .getGroupAuthAccessesCursor(eSortMode.ASC);
        // also closes the old cursor.
        mSimpleCursorTreeAdapter.changeCursor(cursor);
    }

    public void onEvent(ErrorMessageEvent _msg) {
        if (_msg.getErrorMessage() == null) {
            Toast.makeText(mContext, _msg.getResourceId(), Toast.LENGTH_LONG).show();
        }
    }

    public void onEvent(GroupAccessTokenMessageEvent _msg) {
        mTmpGroupId.setText(_msg.getGroupAccessToken());
    }

    public void onEvent(GroupAccessTokenErrorMessageEvent _msg) {
        Toast.makeText(mContext, _msg.mMsg, Toast.LENGTH_SHORT).show();

    }

    public void onEvent(CreateGroupNetworkExceptionMessageEvent _msg) {
        Toast.makeText(mContext,
                _msg.mDeviceName + " attempts: " + _msg.mAttempt,
                Toast.LENGTH_SHORT).show();

    }

    public void onEvent(CreateGroupErrorEvent _msg) {
        Toast.makeText(mContext, _msg.mMessage, Toast.LENGTH_SHORT).show();

    }

    public void onEvent(LocalGroupExistsEvent _msg) {
        Toast.makeText(mContext, R.string.abc_local_group_exists, Toast.LENGTH_LONG).show();
    }

    public void onEvent(HttpResponseCodeErrorMessageEvent _msg) {
        switch (_msg.mCode) {
            case 500:
                Toast.makeText(mContext, R.string.internal_server_error, Toast.LENGTH_LONG).show();
                break;
        }
    }
}
