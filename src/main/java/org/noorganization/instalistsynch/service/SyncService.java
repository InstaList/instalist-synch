package org.noorganization.instalistsynch.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.noorganization.instalistsynch.network.adapter.SyncAdapter;

/**
 * Service that returns an IBinder for the sync adapter class.
 * Allows the sync adapter framework to call onPerformSync().
 * Created by tinos_000 on 05.01.2016.
 */
public class SyncService extends Service {

    /**
     * Reserved for an instance of the syncAdapter.
     */
    private static SyncAdapter msSyncAdapter = null;

    /**
     * This object is used as a thread-safe lock.
     */
    private static final Object msSyncAdapterLock = new Object();

    @Override
    public void onCreate() {
        super.onCreate();
        // create a singleton of syncAdapter
        synchronized (msSyncAdapterLock){
            if(msSyncAdapter == null){
                msSyncAdapter = new SyncAdapter(getApplicationContext(),true, false);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // get the object to call onPerformSync().
        return msSyncAdapter.getSyncAdapterBinder();
    }


}
