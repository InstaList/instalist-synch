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
