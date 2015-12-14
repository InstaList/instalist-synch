package org.noorganization.instalistsynch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.tavendo.autobahn.Autobahn;
import de.tavendo.autobahn.AutobahnConnection;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

interface ISimpleCallback {
    void onMessageReceived(String _msg);
}

public class SynchOverview extends AppCompatActivity implements ISimpleCallback {

    private static String LOG_TAG = SynchOverview.class.getSimpleName();
    private final AutobahnConnection mConnection = new AutobahnConnection();
    private final String mWsUri = "ws://instalist.noorganization.org:80/ws";
    private final String mBaseUrl = "";
    private ISimpleCallback mCallback;

    private TextView mDebugView;
    private Button mRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.noorganization.instalistsynch.R.layout.activity_synch_overview);

        // setup callback to update view
        mCallback = this;
        // assign textview to debug view
        mDebugView = (TextView) this.findViewById(org.noorganization.instalistsynch.R.id.text);
        mRequestButton = (Button) this.findViewById(org.noorganization.instalistsynch.R.id.request_button);

        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start the socket request
                startSocket();
            }
        });

    }


    @Override
    public void onMessageReceived(String _msg) {
        mDebugView.setText(_msg);
    }

    private void startSocket() {

        mConnection.connect(mWsUri, new Autobahn.SessionHandler() {
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
        mConnection.call("instalist/get_tagged_product", String.class, new Autobahn.CallHandler() {
            @Override
            public void onResult(Object _o) {
                String string = (String) _o;
                Log.d(LOG_TAG, "Got echo " + string);
                mCallback.onMessageReceived(string);
            }

            @Override
            public void onError(String s, String s1) {
                Log.d(LOG_TAG, "Closed connection " + s + " addtional info " + s1);
            }
        }, "ABasdjoi-dasd");
    }



}
