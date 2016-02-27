package org.noorganization.instalistsynch.events;

import java.util.Date;

/**
 * Created by Desnoo on 26.02.2016.
 */
public class CategorySynchFromNetworkFinished {

    private Date mLastUpdateDate;
    private int mGroupId;

    public CategorySynchFromNetworkFinished(Date _lastUpdateDate, int _groupId) {
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

