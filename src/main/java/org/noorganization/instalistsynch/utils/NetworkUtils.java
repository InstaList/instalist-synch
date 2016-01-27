package org.noorganization.instalistsynch.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Utils for network interaction.
 * Created by tinos_000 on 27.01.2016.
 */
public class NetworkUtils {

    /**
     * Checks if there is a network connection.
     * @param _context the context of the app.
     * @return true when it is connected to network, else false.
     */
    public static boolean isConnected(Context _context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null  && networkInfo.isConnected();
    }

}
