package org.noorganization.instalistsynch.controller.network.impl;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.noorganization.instalistsynch.controller.network.IServerAuthenticate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Basic implementation to handle auth with the server. Do this only in background
 * @deprecated
 * Created by tinos_000 on 27.01.2016.
 */
public class ServerAuthtentication implements IServerAuthenticate {
    private static final String LOG_TAG = ServerAuthtentication.class.getSimpleName();
    /**
     * Position of the token in the json response.
     */
    private static final int TOKEN_POSITION = 2;

    /**
     * Host url.
     */
    public static String sServerHostUrl = "https://instalist.noorganization.org";
    public static String sServerRelativeSignInUrl = "/signIn";

    private static int sReadTimeOut = 10000; // ms
    private static int sConnectTimeOut = 10000; // ms

    private static ServerAuthtentication mInstance;

    public static ServerAuthtentication getInstance() {
        if (mInstance == null) {
            mInstance = new ServerAuthtentication();
        }
        return mInstance;
    }

    private ServerAuthtentication() {

    }


    @Override
    public String userSignIn(String _name, String _password, String authType) {
        try {
            URL url = new URL(sServerHostUrl.concat(sServerRelativeSignInUrl));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(sReadTimeOut);
            conn.setConnectTimeout(sConnectTimeOut);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.connect();

            int response = conn.getResponseCode();
            Log.d(LOG_TAG, "The response is: " + String.valueOf(response));
            if (response != 200) {
                // TODO catch up other status codes
                return null;
            }

            String responseMsg = conn.getResponseMessage();
            JSONArray responseJSON = new JSONArray(responseMsg);
            return responseJSON.getString(TOKEN_POSITION);

        } catch (IOException e) {
            Log.e(LOG_TAG, "IO exception with uri: " + sServerHostUrl.concat(sServerRelativeSignInUrl));
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to load JSON response.");
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public String userSignUp(String _name, String _email, String _password, String authType) {
        // TODO implement this thing
        return null;
    }
}
