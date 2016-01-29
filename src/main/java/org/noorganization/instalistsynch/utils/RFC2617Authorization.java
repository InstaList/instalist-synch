package org.noorganization.instalistsynch.utils;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

/**
 * Utility to generate a RFC2617 conform authorization header.
 * Created by tinos_000 on 29.01.2016.
 */
public class RFC2617Authorization {
    private static final String LOG_TAG = RFC2617Authorization.class.getSimpleName();

    /**
     * Generates a RFC2617 authorization header.
     *
     * @param _user     the user id.
     * @param _password the password for that user.
     * @return the base64 encoded string with "Basic " in front.
     */
    public static final String generate(String _user, String _password) {
        try {
            // TODO probably change base64 flag
            return "Basic ".concat(Base64.encodeToString(_user.concat(":").concat(_password).getBytes("UTF-8"), Base64.URL_SAFE));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "generate: unsupported encoding", e.getCause());
        }
        return null;
    }
}
