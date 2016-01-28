package org.noorganization.instalistsynch.model;

/**
 * A group with an temporary groupId and a associated secret.
 * Created by tinos_000 on 28.01.2016.
 */
public class Group {
    public String mTempId;
    public String mSecret;

    public Group(String mTempId, String mSecret) {
        this.mTempId = mTempId;
        this.mSecret = mSecret;
    }
}
