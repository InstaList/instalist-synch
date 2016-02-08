package org.noorganization.instalistsynch.events;

/**
 * An error happened while creating a group.
 * Created by tinos_000 on 08.02.2016.
 */
public class CreateGroupErrorEvent {
    public String mMessage;

    public CreateGroupErrorEvent(String _localizedMessage) {
        mMessage = _localizedMessage;
    }
}
