package org.noorganization.instalistsynch.utils;

import android.content.Context;

import org.noorganization.instalistsynch.network.api.IInstantListApiService;

import java.security.SecureRandom;

import retrofit2.JacksonConverterFactory;
import retrofit2.Retrofit;

/**
 * A Global object singleton. Holds objects that only must created once.
 * Created by tinos_000 on 29.01.2016.
 */
public class GlobalObjects {

    /**
     * The Base URL of the API.
     */
    public final static String API_ENDPOINT_URL = "http://instantlist.noorganization.org/v1/";

    /**
     * Instance of this class.
     */
    private static GlobalObjects sInstance;

    private IInstantListApiService mInstantListApiService;

    private SecureRandom mSecureRandom;

    private Context mContext;

    /**
     * Get the instance of this class.
     * @return this GlobalObjects instance.
     */
    public static GlobalObjects getInstance() {
        if(sInstance == null){
            sInstance = new GlobalObjects();
        }
        return sInstance;
    }

    /**
     * Set the application Context.
     * @param _context the context of the application.
     */
    public void setApplicationContext(Context _context){
        mContext = _context;
    }

    /**
     * Get the context of the application.
     * @return the context of the application.
     */
    public Context getApplicationContext(){
        return mContext;
    }
    /**
     * Default private constructor.
     */
    private GlobalObjects() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_ENDPOINT_URL)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        mInstantListApiService = retrofit.create(IInstantListApiService.class);

        mSecureRandom = new SecureRandom();

    }

    /**
     * Get the interface to handle api requests and responses with retrofit.
     * the service to request the api.
     */
    public IInstantListApiService getInstantListApiService(){
        return mInstantListApiService;
    }

    /**
     * Get an instance of SecureRandom.
     * @return the secureRandom instance.
     */
    public SecureRandom getSecureRandom() {
        return mSecureRandom;
    }
}
