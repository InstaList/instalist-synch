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
     */
    public GroupAuth(String _deviceId, String _secret) {
        this.mDeviceId = _deviceId;
        this.mSecret = _secret;
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
         * All column names.
         */
        public static final String ALL_COLUMNS[] = {DEVICE_ID, SECRET};
    }

    public static final String DB_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN.DEVICE_ID + " TEXT PRIMARY KEY NOT NULL, " +
            COLUMN.SECRET + " TEXT NOT NULL" +
            ")";

    /**
     * The generated deviceId.
     */
    private String mDeviceId;

    /**
     * The client secret.
     */
    private String mSecret;

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
}
