package org.noorganization.instalistsynch.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.events.ErrorMessageEvent;
import org.noorganization.instalistsynch.model.network.response.RetrofitAuthToken;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import retrofit2.Response;

/**
 * Utils for network interaction.
 * Created by tinos_000 on 27.01.2016.
 */
public class NetworkUtils {
    private static final String LOG_TAG = NetworkUtils.class.getSimpleName();

    /**
     * Checks if there is a network connection.
     *
     * @param _context the context of the app.
     * @return true when it is connected to network, else false.
     */
    public static boolean isConnected(Context _context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * Checks if the response was succesfull. Sends also the Error with an {@link ErrorMessageEvent} with the {@link EventBus}.
     *
     * @param response the response delivered by retrofit.
     * @return true if it was successful, else false.
     */
    public static boolean isSuccessful(Response<?> response) {
        if (!response.isSuccess()) {
            //noinspection finally
            try {
                String msg = String.valueOf(response.code()).concat(" ").concat(response.errorBody().string());
                EventBus.getDefault().post(new ErrorMessageEvent(msg));
                Log.e(LOG_TAG, "onResponse: server responded with ".concat(msg));
            } catch (IOException e) {
                e.printStackTrace();
                EventBus.getDefault().post(new ErrorMessageEvent(String.valueOf(response.code())
                        .concat(" ").concat(GlobalObjects.getInstance().getApplicationContext()
                                .getString(R.string.network_response_error))));
                Log.e(LOG_TAG, "onResponse: Cannot load body of error message.", e.getCause());
            }
            return true;
        }
        return false;
    }

}
