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
    private String mTmpGroupId;

    /**
     * The name of the device.
     */
    private String _deviceName;

    /**
     * Indicates if this group is the group that was created locally.
     */
    private boolean _isLocal;
    /**
     * Indicates if the join group operation was a success or not.
     */
    private boolean mSuccess;
    /**
     * The id of the device.
     */
    private int mDeviceId;


    public GroupJoin(String tmpGroupId, String _deviceName, boolean _isLocal, boolean success, int deviceId) {
        mTmpGroupId = tmpGroupId;
        this._deviceName = _deviceName;
        this._isLocal = _isLocal;
        mSuccess = success;
        mDeviceId = deviceId;
    }

    public final String getTmpGroupId() {
        return mTmpGroupId;
    }

    public final String get_deviceName() {
        return _deviceName;
    }

    public final boolean is_isLocal() {
        return _isLocal;
    }

    public final boolean isSuccess() {
        return mSuccess;
    }

    public final int getDeviceId() {
        return mDeviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GroupJoin groupJoin = (GroupJoin) o;

        if (_isLocal != groupJoin._isLocal) return false;
        if (mSuccess != groupJoin.mSuccess) return false;
        if (mDeviceId != groupJoin.mDeviceId) return false;
        if (mTmpGroupId != null ? !mTmpGroupId.equals(groupJoin.mTmpGroupId) : groupJoin.mTmpGroupId != null)
            return false;
        return !(_deviceName != null ? !_deviceName.equals(groupJoin._deviceName) : groupJoin._deviceName != null);

    }

    @Override
    public int hashCode() {
        int result = mTmpGroupId != null ? mTmpGroupId.hashCode() : 0;
        result = 31 * result + (_deviceName != null ? _deviceName.hashCode() : 0);
        result = 31 * result + (_isLocal ? 1 : 0);
        result = 31 * result + (mSuccess ? 1 : 0);
        result = 31 * result + mDeviceId;
        return result;
    }
}
