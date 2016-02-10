package org.noorganization.instalistsynch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.noorganization.instalistsynch.network.auth.Authenticator;


/**
 * A bound service that instantiates the authenticator when started.
 * Is called by Accountmanager to get a token for example.
 * Created by tinos_000 on 05.01.2016.
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator = new Authenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }

}
