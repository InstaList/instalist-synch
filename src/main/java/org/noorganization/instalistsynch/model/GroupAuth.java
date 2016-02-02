package org.noorganization.instalistsynch.model;

/**
 * Object model to get access token to a group.
 * Created by tinos_000 on 29.01.2016.
 */
public class GroupAuth {

    /**
     * Basic constructor.
     */
    public GroupAuth() {
    }

    /**
     * Constructor for instant creation.
     *
     * @param _deviceId the id of the device.
     * @param _secret   the client secret.
     * @param _name  the name of the specified device.
     * @param _isLocal true if it is locally created or false if group is from host.
     */
    public GroupAuth(String _deviceId, String _secret, String _name, boolean _isLocal) {
        this.mDeviceId = _deviceId;
        this.mSecret = _secret;
        this.mDeviceName = _name;
        this.mIsLocal = _isLocal;
    }

    public final static String TABLE_NAME = "groupauth";

    /**
     * Holder of Column names.
     */
    public final static class COLUMN {
        /**
         * The column name of DeviceId.
         */
        public static final String DEVICE_ID = "device_id";
        /**
         * The column name of secret.
         */
        public static final String SECRET = "secret";

        /**
         * The name of the specific deivce.
         */
        public static final String DEVICE_NAME = "device_name";

        /**
         * The name of the is local field.
         */
        public static final String IS_LOCAL = "is_local";
        /**
         * All column names.
         */
        public static final String ALL_COLUMNS[] = {DEVICE_ID, SECRET, DEVICE_NAME, IS_LOCAL};
    }

    /**
     * String to create table of group auth.
     */
    public static final String DB_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN.DEVICE_ID + " TEXT PRIMARY KEY NOT NULL, " +
            COLUMN.SECRET + " TEXT NOT NULL," +
            COLUMN.DEVICE_NAME + " TEXT NOT NULL," +
            COLUMN.IS_LOCAL + " INTEGER NOT NULL" +
            ")";

    /**
     * The generated deviceId.
     */
    private String mDeviceId;

    /**
     * The client secret.
     */
    private String mSecret;

    /**
     * The device name.
     */
    private String mDeviceName;

    /**
     * Indicates if this device_id is the owner of the group. Prevent duplication insertion.
     */
    private boolean mIsLocal;

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String _deviceId) {
        this.mDeviceId = _deviceId;
    }

    public String getSecret() {
        return mSecret;
    }

    public void setSecret(String _secret) {
        this.mSecret = _secret;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public boolean isLocal() {
        return mIsLocal;
    }

    public void setIsOwner(boolean isOwner) {
        mIsLocal = isOwner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupAuth)) return false;

        GroupAuth groupAuth = (GroupAuth) o;

        if (isLocal() != groupAuth.isLocal()) return false;
        if (!getDeviceId().equals(groupAuth.getDeviceId())) return false;
        if (!getSecret().equals(groupAuth.getSecret())) return false;
        return getDeviceName().equals(groupAuth.getDeviceName());

    }

    @Override
    public int hashCode() {
        int result = getDeviceId().hashCode();
        result = 31 * result + getSecret().hashCode();
        result = 31 * result + getDeviceName().hashCode();
        result = 31 * result + (isLocal() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GroupAuth{" +
                "mDeviceId='" + mDeviceId + '\'' +
                ", mSecret='" + mSecret + '\'' +
                ", mDeviceName='" + mDeviceName + '\'' +
                ", mIsLocal=" + mIsLocal +
                '}';
    }
}
