package org.noorganization.instalistsynch.model.observable;

/**
 * The model used to sent on the stream, when the join group network operation was done.
 * It is read only.
 * Created by Desnoo on 07.02.2016.
 */
public class GroupJoin {


    /**
     * The temporary group id.
     */
    private int mGroupId;

    /**
     * The name of the device.
     */
    private String mDeviceName;

    /**
     * The id of the device.
     */
    private int mDeviceId;


    public GroupJoin(int _groupId, String _deviceName, int _deviceId) {
        mGroupId = _groupId;
        mDeviceName = _deviceName;
        mDeviceId = _deviceId;
    }


    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(int deviceId) {
        mDeviceId = deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupJoin)) return false;

        GroupJoin groupJoin = (GroupJoin) o;

        if (getGroupId() != groupJoin.getGroupId()) return false;
        if (getDeviceId() != groupJoin.getDeviceId()) return false;
        return !(getDeviceName() != null ? !getDeviceName().equals(groupJoin.getDeviceName()) : groupJoin.getDeviceName() != null);

    }

    @Override
    public int hashCode() {
        int result = getGroupId();
        result = 31 * result + (getDeviceName() != null ? getDeviceName().hashCode() : 0);
        result = 31 * result + getDeviceId();
        return result;
    }
}
