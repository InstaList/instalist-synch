package org.noorganization.instalistsynch.adapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

/**
 * The SyncAdapter handles the transfer of data between the client and the server. Also resolves conflicts.
 * Created by tinos_000 on 05.01.2016.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /**
     * Instance of an ContentResolver.
     */
    private ContentResolver mContentResolver;

    /**
     * Set up of the SyncAdapter. Specially for android 3.0+ versions.
     * @param _context the context of the app.
     * @param _autoInitialize
     * @param _allowParallelSyncs
     */
    public SyncAdapter(Context _context, boolean _autoInitialize, boolean _allowParallelSyncs) {
        super(_context, _autoInitialize, _allowParallelSyncs);
        // init the contentResolver
        mContentResolver = _context.getContentResolver();
    }

    // Runs in background process.
    @Override
    public void onPerformSync(Account _account, Bundle _extras, String _authority, ContentProviderClient _provider, SyncResult _syncResult) {
        // TODO: Data synch in here !

    }
}
