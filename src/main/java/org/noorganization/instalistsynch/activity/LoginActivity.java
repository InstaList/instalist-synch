package org.noorganization.instalistsynch.activity;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.noorganization.instalistsynch.R;
import org.noorganization.instalistsynch.controller.IServerAuthenticate;
import org.noorganization.instalistsynch.controller.impl.ServerAuthenticationFactory;
import org.noorganization.instalistsynch.controller.impl.ServerAuthtentication;
import org.noorganization.instalistsynch.utils.NetworkUtils;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AccountAuthenticatorActivity {

    /**
     * Key for type of account.
     */
    public static final String KEY_ACCOUNT_TYPE = "account_type";
    /**
     * Key for auth type.
     */
    public static final String KEY_AUTH_TYPE = "auth_type";
    /**
     * Key for is new account.
     */
    public static final String KEY_IS_ADDING_NEW_ACCOUNT_TYPE = "is_new_account";
    /**
     * The standard type of a user, no special thing.
     */
    private static final String STANDARD_USER_TYPE = "standard_user";


    /**
     * Handles the sign in and sign up process.
     */
    private IServerAuthenticate mAuthenticator;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    /**
     * The auth type of the token.
     */
    private final String mAuthTokenType = "AUTH_TOKEN_TODO";

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private AccountManager mAccountManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAccountManager = AccountManager.get(this);
        mAuthenticator = ServerAuthenticationFactory.getDefaultServerAuthentication();

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }


    private void showProgress(boolean _show) {
        if (_show)
            mProgressView.setVisibility(View.VISIBLE);
        else
            mProgressView.setVisibility(View.GONE);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            if(!NetworkUtils.isConnected(this)) {
                mAuthTask.execute((Void) null);
            } else {
                Toast.makeText(this, R.string.no_internet_conn, Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void finishLogin(Intent _intent) {
        String accountName = _intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = _intent.getStringExtra(AccountManager.KEY_PASSWORD);
        final Account account = new Account(accountName, _intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if(getIntent().getBooleanExtra(KEY_IS_ADDING_NEW_ACCOUNT_TYPE, false)){
            // if account is newly added
            String authToken = _intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authTokenType = mAuthTokenType;
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account,authTokenType, authToken);
        } else {
            mAccountManager.setPassword(account, accountPassword);
        }
        setAccountAuthenticatorResult(_intent.getExtras());
        setResult(RESULT_OK, _intent);
        finish();
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Intent> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Intent doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            String authToken = mAuthenticator.userSignIn(mEmail, mPassword, mAuthTokenType);
            final Intent result = new Intent();
            result.putExtra(AccountManager.KEY_ACCOUNT_NAME, mEmail);
            result.putExtra(AccountManager.KEY_ACCOUNT_TYPE, STANDARD_USER_TYPE);
            result.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
            result.putExtra(AccountManager.KEY_PASSWORD, mPassword);
            return result;
        }

        @Override
        protected void onPostExecute(final Intent _result) {
            mAuthTask = null;
            showProgress(false);
            finishLogin(_result);
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


}

