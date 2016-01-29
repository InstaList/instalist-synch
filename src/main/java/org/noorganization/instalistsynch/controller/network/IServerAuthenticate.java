package org.noorganization.instalistsynch.controller.network;

/**
 * Interface to provide authentication methods to the server.
 * Created by tinos_000 on 27.01.2016.
 */
public interface IServerAuthenticate {
    String userSignUp(String _name,String _email,  String _password, String authType);
    String userSignIn(String _name, String _password, String authType);
}
