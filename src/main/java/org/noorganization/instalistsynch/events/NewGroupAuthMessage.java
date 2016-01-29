package org.noorganization.instalistsynch.events;

import org.noorganization.instalistsynch.model.GroupAuth;

/**
 * Message when a new group access was established.
 * Created by tinos_000 on 29.01.2016.
 */
public class NewGroupAuthMessage {

    /**
     * The group auth object.
     */
    private GroupAuth mGroupAuth;

    /**
     * Basic Constructor.
     *
     * @param mGroupAuth the group auth object.
     */
    public NewGroupAuthMessage(GroupAuth mGroupAuth) {
        this.mGroupAuth = mGroupAuth;
    }

    /**
     * Get the group auth object.
     *
     * @return the groupauth object.
     */
    public GroupAuth getGroupAuth() {
        return mGroupAuth;
    }

}
