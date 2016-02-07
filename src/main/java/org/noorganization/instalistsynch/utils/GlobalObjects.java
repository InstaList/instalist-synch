package org.noorganization.instalistsynch.utils;

import android.content.Context;

import java.security.SecureRandom;

/**
 * A Global object singleton. Holds objects that only must created once.
 * Created by tinos_000 on 29.01.2016.
 */
public class GlobalObjects {

    /**
     * Instance of this class.
     */
    private static GlobalObjects sInstance;


    /**
     * The securerandom instance.
     */
    private SecureRandom mSecureRandom;

    /**
     * Context of the app.
     */
    private Context mContext;

    /**
     * Get the instance of this class.
     *
     * @return this GlobalObjects instance.
     */
    public static GlobalObjects getInstance() {
        if (sInstance == null) {
            sInstance = new GlobalObjects();
        }
        return sInstance;
    }

    /**
     * Set the application Context.
     *
     * @param _context the context of the application.
     */
    public void setApplicationContext(Context _context) {
        mContext = _context;
    }

    /**
     * Get the context of the application.
     *
     * @return the context of the application.
     */
    public Context getApplicationContext() {
        return mContext;
    }

    /**
     * Default private constructor.
     */
    private GlobalObjects() {
        mSecureRandom = new SecureRandom();

    }

    /**
     * Get an instance of SecureRandom.
     *
     * @return the secureRandom instance.
     */
    public SecureRandom getSecureRandom() {
        return mSecureRandom;
    }
}
