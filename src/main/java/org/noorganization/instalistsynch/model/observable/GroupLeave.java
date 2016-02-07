package org.noorganization.instalistsynch.model.observable;

/**
 * The object emitted filled with the data of a group and if it was successful on server.
 * Created by Desnoo on 07.02.2016.
 */
public class GroupLeave {
    /**
     * The group id.
     */
    private int mGroupId;

    /**
     * Indicates if the leave group operation was a success or not.
     */
    private boolean mSuccess;

    /**
     * The id of the device.
     */
    private int mDeviceId;

    public GroupLeave(int groupId, boolean success, int deviceId) {
        mGroupId = groupId;
        mSuccess = success;
        mDeviceId = deviceId;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public boolean isSuccess() {
        return mSuccess;
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupLeave that = (GroupLeave) o;

        if (mGroupId != that.mGroupId) return false;
        if (mSuccess != that.mSuccess) return false;
        return mDeviceId == that.mDeviceId;

    }

    @Override
    public int hashCode() {
        int result = mGroupId;
        result = 31 * result + (mSuccess ? 1 : 0);
        result = 31 * result + mDeviceId;
        return result;
    }
}
