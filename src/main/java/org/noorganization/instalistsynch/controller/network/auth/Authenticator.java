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

package org.noorganization.instalistsynch.controller.network.auth;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.noorganization.instalistsynch.view.activity.LoginActivity;
import org.noorganization.instalistsynch.controller.network.IServerAuthenticate;
import org.noorganization.instalistsynch.controller.network.impl.ServerAuthenticationFactory;

/**
 * Created by tinos_000 on 05.01.2016.
 * Implement AbstractAccountAuthenticator and stub out all
 * of its methods
 */
public class Authenticator extends AbstractAccountAuthenticator {

    /**
     * The context of the application.
     */
    private Context mContext;

    /**
     * Handles the sign in and sign up process.
     */
    private IServerAuthenticate mAuthenticator;


    // Simple constructor
    public Authenticator(Context context) {
        super(context);
        mContext = context;
        mAuthenticator = ServerAuthenticationFactory.getDefaultServerAuthentication();
    }

    // Editing properties is not supported
    @Override
    public Bundle editProperties(
            AccountAuthenticatorResponse r, String s) {
        // add here an intent creation with a bundle with params to edit them in a activity
        throw new UnsupportedOperationException();
    }

    // add additional accounts should be possible.
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse _response, String _accountType, String _authTokenType, String[] _requiredFeatures, Bundle _options) throws NetworkErrorException {

        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(LoginActivity.KEY_ACCOUNT_TYPE, _accountType);
        intent.putExtra(LoginActivity.KEY_AUTH_TYPE, _authTokenType);
        intent.putExtra(LoginActivity.KEY_IS_ADDING_NEW_ACCOUNT_TYPE, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, _response);

        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    // Ignore attempts to confirm credentials
    // used if you want to do verification with a gmail account for example
    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            Bundle bundle) throws NetworkErrorException {
        return null;
    }

    // Getting an authentication token is not supported
    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse _response, Account _account, String _authTokenType, Bundle _options) throws NetworkErrorException {

        // get the username and password from Account Manager and also ask for an AuthToken
        final AccountManager accountManager = AccountManager.get(mContext);
        String authToken = accountManager.peekAuthToken(_account, _authTokenType);

        if(TextUtils.isEmpty(authToken)){
            final String password = accountManager.getPassword(_account);
            if(password != null){
                authToken = mAuthenticator.userSignIn(_account.name, password, _authTokenType);
            }
        }

        // if the auth token is set, return the bundle with all neccessary data
        if(!TextUtils.isEmpty(authToken)){
            final Bundle result = new Bundle();
            result.putString(AccountManager.KEY_ACCOUNT_NAME, _account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, _account.type);
            result.putString(LoginActivity.KEY_AUTH_TYPE, _authTokenType);
            result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
            return result;
        }

        // no auth token taken, because password cannot be taken
        // prompt user with login screen again
        final Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra( AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, _response);
        intent.putExtra(LoginActivity.KEY_ACCOUNT_TYPE, _account.type);
        intent.putExtra(LoginActivity.KEY_AUTH_TYPE, _authTokenType);
        // name cannot be null, because each account has a name
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, _account.name);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    // Getting a label for the auth token is not supported
    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }

    // Updating user credentials is not supported
    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            String s, Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    // Checking features for the account is not supported
    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse r,
            Account account, String[] strings) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
