package org.noorganization.instalistsynch.activity;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.network.impl.V1GroupManager;
import org.noorganization.instalistsynch.events.ErrorMessage;
import org.noorganization.instalistsynch.events.TokenMessage;
import org.noorganization.instalistsynch.utils.AccountUtils;

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

    private EditText mLoginUsername;
    private EditText mLoginPassword;
    private Button mLoginButton;

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.noorganization.instalistsynch.R.layout.activity_synch_overview);

        mContext = this;
        EventBus.getDefault().register(this);

        // assign textview to debug view
        mDebugView = (TextView) this.findViewById(R.id.text);
        mRequestButton = (Button) this.findViewById(R.id.request_button);

        mLoginUsername = (EditText) this.findViewById(R.id.login_username_edit_text);
        mLoginPassword = (EditText) this.findViewById(R.id.login_password_edit_text);
        mLoginButton = (Button) this.findViewById(R.id.login_submit);

        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the socket request
                V1GroupManager.getInstance().createGroup();
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = AccountUtils.createSyncAccount(getApplicationContext(), mLoginUsername.getText().toString());
                if (account == null) {
                    Toast.makeText(mContext, getString(R.string.abc_username_exists), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void onEvent(ErrorMessage _msg){
        Toast.makeText(this,_msg.getErrorMessage(), Toast.LENGTH_LONG);
        Log.e(LOG_TAG, "onEvent: " + _msg.getErrorMessage());
    }

    public void onEvent(TokenMessage _msg){
        Toast.makeText(this,"Token: " + _msg.getmToken(), Toast.LENGTH_LONG);
        Log.i(LOG_TAG, "onEvent: " + _msg.getmToken());
    }

}
