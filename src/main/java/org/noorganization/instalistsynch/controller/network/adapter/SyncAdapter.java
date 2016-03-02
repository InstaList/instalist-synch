/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.network.adapter;

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
     * @param _autoInitialize true if it should, false if not auto initialize.
     * @param _allowParallelSyncs allows parallel syncs or not.
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
