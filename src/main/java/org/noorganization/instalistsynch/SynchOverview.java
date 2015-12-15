package org.noorganization.instalistsynch;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
