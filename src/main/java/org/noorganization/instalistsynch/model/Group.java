package org.noorganization.instalistsynch.model;

/**
 * A group with an temporary groupId and a associated secret.
 * Created by tinos_000 on 28.01.2016.
 */
public class Group {
    public String groupid;
    public String secret;
    public String name;
    /**
     * The group that holds the temporary group id.
     * @param _groupId the temporary group id.
     * @param _secret the generated secret.
     * @param _name the name of the device.
     */
    public Group(String _groupId, String _secret, String _name) {
        this.groupid = _groupId;
        this.secret = _secret;
        this.name = _name;
    }
}
