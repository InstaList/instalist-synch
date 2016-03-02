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

package org.noorganization.instalistsynch.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by tinos_000 on 05.01.2016.
 */
public class AccountUtils {

    private static final String TAG = "AccountUtils";

    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "org.noorganization.instalistsynch";

    /**
     * Create a account for the sync adapter.
     *
     * @param _context The application context.
     * @return the new created Account.
     */
    public static Account createSyncAccount(Context _context, String _username) {
        // create a new a account.
        Account newAccount = new Account(_username, ACCOUNT_TYPE);

        AccountManager accountManager = (AccountManager) _context.getSystemService(Context.ACCOUNT_SERVICE);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        for (Account account : accounts) {
            if (account.equals(newAccount)) {
                // account already exists
                return null;
            }
        }

        // try to login and retrieve the token

        Bundle bundle = new Bundle();
        bundle.putString("Token", "some auth token please");

        if (accountManager.addAccountExplicitly(newAccount, null, bundle)) {

        } else {
            Log.i(TAG, "createSyncAccount: account already exists or an error happened");
        }

        return newAccount;
    }
}
