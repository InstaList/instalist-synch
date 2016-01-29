package org.noorganization.instalistsynch.controller.network.impl;

import org.noorganization.instalistsynch.controller.network.IServerAuthenticate;

/**
 * Factory to manage the possible network objects.
 * Created by tinos_000 on 27.01.2016.
 */
public class ServerAuthenticationFactory {

    /**
     * Get the default implementation of the authentification with the server.
     * @return the Authenticator instance.
     */
    public static IServerAuthenticate getDefaultServerAuthentication(){
        return ServerAuthtentication.getInstance();
    }
}
