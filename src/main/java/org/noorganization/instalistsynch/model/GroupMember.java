package org.noorganization.instalistsynch.model;

/**
 * Represents a member of a group.
 * Created by Desnoo on 01.02.2016.
 */
public class GroupMember {

    /**
     * The uuid of this group member.
     */
    private String mUUID;

    /**
     * The device id of each device.
     */
    private String mDeviceId;
    /**
     * Used to indicate group association.
     */
    private String mOwnDeviceId;
    private String mName;
    private boolean mAuthorized;


    public final static class COLUMN {
        public static final String ID = "uuid";
        public static final String OWN_DEVICE_ID = "own_device_id";
        public static final String DEVICE_ID = "device_id";
        public static final String NAME = "name";
        public static final String AUTHORIZED = "authorized";
        public static final String ALL_COLUMNS[] = {ID, OWN_DEVICE_ID, DEVICE_ID, NAME, AUTHORIZED};
    }

    public static final String TABLE_NAME = "group_member";

    public static String DB_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN.ID + " TEXT PRIMARY KEY NOT NULL, " +
            COLUMN.OWN_DEVICE_ID + " TEXT NOT NULL, " +
            COLUMN.DEVICE_ID + " TEXT NOT NULL, " +
            COLUMN.AUTHORIZED + " INTEGER NOT NULL, " +
            COLUMN.NAME + " TEXT NOT NULL, " +
            "FOREIGN KEY (" + COLUMN.OWN_DEVICE_ID + ") REFERENCES " + GroupAuth.TABLE_NAME + " ( " +
            GroupAuth.COLUMN.DEVICE_ID + ") ON UPDATE CASCADE ON DELETE CASCADE" +
            ")";

    public GroupMember() {
    }

    public GroupMember(String UUID, String deviceId, String ownDeviceId, String name, boolean authorized) {
        mUUID = UUID;
        mDeviceId = deviceId;
        mOwnDeviceId = ownDeviceId;
        mName = name;
        mAuthorized = authorized;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String UUID) {
        mUUID = UUID;
    }

    public String getOwnDeviceId() {
        return mOwnDeviceId;
    }

    public void setOwnDeviceId(String ownDeviceId) {
        mOwnDeviceId = ownDeviceId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isAuthorized() {
        return mAuthorized;
    }

    public void setAuthorized(boolean authorized) {
        mAuthorized = authorized;
    }
}
