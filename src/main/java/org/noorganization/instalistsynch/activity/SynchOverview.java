package org.noorganization.instalistsynch.activity;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.TaggedProduct;
import org.noorganization.instalistsynch.utils.AccountUtils;

import de.tavendo.autobahn.Wamp;
import de.tavendo.autobahn.WampConnection;


interface ISimpleCallback {
    void onMessageReceived(String _msg);
}

public class SynchOverview extends AppCompatActivity implements ISimpleCallback {

    private static String LOG_TAG = SynchOverview.class.getSimpleName();
    private final Wamp mConnection = new WampConnection();
    private final String mWsUri = "ws://instalist.noorganization.org:80/ws";
    private final String mBaseUrl = "";
    private ISimpleCallback mCallback;

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
        // setup callback to update view
        mCallback = this;
        // assign textview to debug view
        mDebugView = (TextView) this.findViewById(R.id.text);
        mRequestButton = (Button) this.findViewById(R.id.request_button);

        mLoginUsername = (EditText) this.findViewById(R.id.login_username_edit_text);
        mLoginPassword = (EditText) this.findViewById(R.id.login_password_edit_text);
        mLoginButton   = (Button) this.findViewById(R.id.login_submit);

        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the socket request
                startSocket();
            }
        });

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = AccountUtils.createSyncAccount(getApplicationContext(), mLoginUsername.getText().toString());
                if(account == null){
                    Toast.makeText(mContext, getString(R.string.abc_username_exists), Toast.LENGTH_SHORT).show();
                }

            }
        });


    }


    @Override
    public void onMessageReceived(String _msg) {
        mDebugView.setText(_msg);
    }

    private void startSocket() {

        mConnection.connect(mWsUri, new Wamp.ConnectionHandler() {
            @Override
            public void onOpen() {
                Log.d(LOG_TAG, "Connected to " + mWsUri);
                rpcCall();
            }

            @Override
            public void onClose(int i, String s) {
                Log.d(LOG_TAG, "Closed connection " + s);
            }
        });

    }

    private void rpcCall(){
        mConnection.call("instalist/get_tagged_product", TaggedProduct.class, new Wamp.CallHandler() {
            @Override
            public void onResult(Object _o) {
                TaggedProduct testTaggedProduct = (TaggedProduct) _o;
                Log.d(LOG_TAG, "Got echo " + testTaggedProduct);
                mCallback.onMessageReceived(testTaggedProduct.toString());
            }

            @Override
            public void onError(String s, String s1) {
                Log.d(LOG_TAG, "Closed connection " + s + " addtional info " + s1);
            }
        }, "ABasdjoi-dasd");
    }



}
