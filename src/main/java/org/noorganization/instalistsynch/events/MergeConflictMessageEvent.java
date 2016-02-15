package org.noorganization.instalistsynch.events;

/**
 * Sent when there was a merge conflict that the user should solve.
 * Created by Desnoo on 15.02.2016.
 */
public class MergeConflictMessageEvent {

    public int mConflictId;
    public String mUUID;

    public MergeConflictMessageEvent(int _conflictId, String _uuid) {
        mConflictId = _conflictId;
        mUUID = _uuid;
    }
}
