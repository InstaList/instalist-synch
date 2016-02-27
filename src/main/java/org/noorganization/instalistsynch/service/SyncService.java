package org.noorganization.instalistsynch.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.noorganization.instalistsynch.controller.network.adapter.SyncAdapter;
import org.noorganization.instalistsynch.controller.synch.SynchManager;

/**
 * Service that returns an IBinder for the sync adapter class.
 * Allows the sync adapter framework to call onPerformSync().
 * Created by tinos_000 on 05.01.2016.
 */
public class SyncService extends Service {
    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    private SynchManager mSynchManager;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SyncService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SyncService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSynchManager = new SynchManager();
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // create a singleton of syncAdapter

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // get the object to call onPerformSync().
        return mBinder;
    }


    public SynchManager getSynchManager(){
        return mSynchManager;
    }



}
