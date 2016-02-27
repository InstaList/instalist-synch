package org.noorganization.instalistsynch.events;

import java.util.Date;

/**
 * Sent when the synch from network for the list was finished.
 * Created by Desnoo on 26.02.2016.
 */
public class ListSynchFromNetworkFinished {

    private Date mLastUpdateDate;
    private int mGroupId;

    public ListSynchFromNetworkFinished(Date _lastUpdateDate, int _groupId) {
        mLastUpdateDate = _lastUpdateDate;
        mGroupId = _groupId;
    }

    public Date getLastUpdateDate() {
        return mLastUpdateDate;
    }

    public int getGroupId() {
        return mGroupId;
    }
}

