package org.noorganization.instalistsynch.controller.impl;

import org.noorganization.instalistsynch.controller.IServerAuthenticate;

/**
 * Basic implementation to handle auth with the server.
 * Created by tinos_000 on 27.01.2016.
 */
public class ServerAuthtentication implements IServerAuthenticate {

    @Override
    public String userSignIn(String _name, String _password, String authType) {
        return null;
    }

    @Override
    public String userSignUp(String _name, String _email, String _password, String authType) {
        return null;
    }
}
