package org.noorganization.instalistsynch.events;

/**
 * Indicates if joining group was successful or not.
 * Created by Desnoo on 03.02.2016.
 */
public class GroupJoinedMessageEvent {
    public boolean mJoined;

    public GroupJoinedMessageEvent(boolean joined) {
        mJoined = joined;
    }
}
