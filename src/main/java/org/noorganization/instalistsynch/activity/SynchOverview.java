package org.noorganization.instalistsynch.activity;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
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
import org.noorganization.instalistsynch.events.GroupMemberListMessageEvent;
import org.noorganization.instalistsynch.events.TokenMessageEvent;
import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.GroupExpandableList;
import org.noorganization.instalistsynch.model.GroupMember;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.WampConnection;


public class SynchOverview extends AppCompatActivity {

    private static String LOG_TAG = SynchOverview.class.getSimpleName();
    private final Wamp mConnection = new WampConnection();
    private final String mWsUri = "ws://instalist.noorganization.org:80/ws";
    private final String mBaseUrl = "";

    private TextView mDebugView;
    private Button mRequestButton;

    private EditText mDeviceNameInput;
    private Button mLoginButton;

    private Context mContext;
    private ArrayAdapter<GroupAuth> mAdapter;

    private ExpandableListView mExpandableListView;
    private List<GroupExpandableList> mGroupExpandableLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.noorganization.instalistsynch.R.layout.activity_synch_overview);

        mContext = this;
        EventBus.getDefault().register(this);

        mExpandableListView = (ExpandableListView) this.findViewById(R.id.testListView);
        // assign textview to debug view
        mDebugView = (TextView) this.findViewById(R.id.text);
        mRequestButton = (Button) this.findViewById(R.id.request_button);

        mDeviceNameInput = (EditText) this.findViewById(R.id.login_username_edit_text);
        mLoginButton = (Button) this.findViewById(R.id.login_submit);

        mRequestButton.setOnClickListener(new View.OnClickListener() {
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

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SynchDbHelper dbHelper = new SynchDbHelper(mContext);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(GroupMember.TABLE_NAME, null, null);
                db.delete(GroupAuth.TABLE_NAME, null, null);
                db.delete(GroupAuthAccess.TABLE_NAME, null, null);
                mAdapter.clear();
                populateListAdapter();
            }
        });

        /*
        mExpandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                GroupAuthAccess groupAuthAccess = ((GroupAuthAccess) mExpandableListView.getItemAtPosition(position));
                IGroupAuthDbController authAccessDbController = LocalControllerFactory.getDefaultAuthController(mContext);
                IGroupManager groupManager = NetworkControllerFactory.getGroupManager();
                groupManager.requestGroupAccessToken(groupAuthAccess.getToken());
                return false;
            }
        });

        mExpandableListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.action_menu, menu);

                ExpandableListView listView = (ExpandableListView) v;
                AdapterView.AdapterContextMenuInfo adapterInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
                listView.getItemAtPosition(adapterInfo.position);
            }
        });*/

        //populateListAdapter();

        IGroupAuthAccessDbController authAccessDbController = LocalControllerFactory.getSqliteAuthAccessController(mContext);
        IGroupAuthDbController groupAuthDbController = LocalControllerFactory.getDefaultAuthController(mContext);
        IGroupManager groupManager = NetworkControllerFactory.getGroupManager();

        List<GroupAuth> groupAuthList = groupAuthDbController.getRegisteredGroups();
        for (GroupAuth groupAuth : groupAuthList) {
            groupManager.requestAuthToken(groupAuth);
        }

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
        Toast.makeText(this, _msg.getErrorMessage(), Toast.LENGTH_LONG);
        mDebugView.setText(mDebugView.getText().toString().concat("\n  ").concat(_msg.getErrorMessage()));
        Log.e(LOG_TAG, "onEvent: " + _msg.getErrorMessage());
    }

    public void onEvent(TokenMessageEvent _msg) {
        Toast.makeText(this, "Token: " + _msg.getmToken(), Toast.LENGTH_LONG);
        mDebugView.setText(mDebugView.getText().toString().concat("\n  ").concat(_msg.getmToken()));
        Log.i(LOG_TAG, "onEvent: " + _msg.getmToken());
        IGroupManager groupManager = NetworkControllerFactory.getGroupManager();
        groupManager.getGroupMembers(_msg.getmToken());
        // populateListAdapter();
    }

    public void onEvent(GroupAccessTokenMessageEvent _msg) {
        mDebugView.setText(mContext.getString(R.string.abc_temporary_access_token).concat(" ").concat(_msg.getGroupAccessToken()));
    }

    public void onEvent(GroupMemberListMessageEvent _msg) {
        for (GroupExpandableList groupExpandableList : mGroupExpandableLists) {
            if (groupExpandableList.getGroupAuthAccess().getDeviceId().equals(_msg.getDeviceId())) {
                groupExpandableList.setGroupMemberList(_msg.getGroupMembers());
            }
        }
        mExpandableListView.setAdapter(new GroupExpandableListAdapter(mGroupExpandableLists, mContext));

    }


}
