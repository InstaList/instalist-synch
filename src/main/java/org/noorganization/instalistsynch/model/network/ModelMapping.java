package org.noorganization.instalistsynch.model.network;

import java.util.Date;

/**
 * Mapping of each model, it is done in a common way.
 * Created by tinos_000 on 30.01.2016.
 */
public class ModelMapping {
    public String mUUID;
    public String mDeviceId;
    public String mServerSideUUID;
    public String mClientSideUUID;
    public Date mLastServerChanged;
    public Date mLastClientChange;

    public final static class COLUMN {
        public final static String ID = "uuid";
        public final static String DEVICE_ID = "device_id";
        public final static String SERVER_SIDE_UUID = "server_side_uuid";
        public final static String CLIENT_SIDE_UUID = "client_side_uuid";
        public final static String LAST_SERVER_CHANGE = "last_server_change";
        public final static String LAST_CLIENT_CHANGE = "last_server_change";

        public final static String[] ALL_COLUMNS = {ID, DEVICE_ID, SERVER_SIDE_UUID, CLIENT_SIDE_UUID, LAST_SERVER_CHANGE, LAST_CLIENT_CHANGE};
    }

    public static final String SHOPPING_LIST_MAPPING_TABLE_NAME = "shopping_list_mapping";
    public static final String DB_CREATE_SHOPPING_LIST_MAPPING = "CREATE TABLE " + SHOPPING_LIST_MAPPING_TABLE_NAME + "(" +
            COLUMN.ID + " TEXT PRIMARY KEY NOT NULL, " +
            COLUMN.DEVICE_ID + " TEXT NOT NULL," +
            COLUMN.SERVER_SIDE_UUID + " TEXT," +
            COLUMN.CLIENT_SIDE_UUID + " TEXT," +
            COLUMN.LAST_SERVER_CHANGE + " TEXT NOT NULL default '2000-01-01T00:01:00+01:00'," +
            COLUMN.LAST_CLIENT_CHANGE + " TEXT NOT NULL default '2000-01-01T00:01:00+01:00'"
            + ")";

    /**
     * Constructor of ModelMapping
     * @param UUID the uuid of the mapping.
     * @param deviceId the device id (group id)
     * @param serverSideUUID the uuid on server side.
     * @param clientSideUUID the uuid on client side.
     * @param lastServerChanged the date when the server has made the last change.
     * @param lastClientChange the date when the client has made the last change.
     */
    public ModelMapping(String UUID, String deviceId, String serverSideUUID, String clientSideUUID, Date lastServerChanged, Date lastClientChange) {
        mUUID = UUID;
        mDeviceId = deviceId;
        mServerSideUUID = serverSideUUID;
        mClientSideUUID = clientSideUUID;
        mLastServerChanged = lastServerChanged;
        mLastClientChange = lastClientChange;
    }

    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String UUID) {
        mUUID = UUID;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getServerSideUUID() {
        return mServerSideUUID;
    }

    public void setServerSideUUID(String serverSideUUID) {
        mServerSideUUID = serverSideUUID;
    }

    public String getClientSideUUID() {
        return mClientSideUUID;
    }

    public void setClientSideUUID(String clientSideUUID) {
        mClientSideUUID = clientSideUUID;
    }

    public Date getLastServerChanged() {
        return mLastServerChanged;
    }

    public void setLastServerChanged(Date lastServerChanged) {
        mLastServerChanged = lastServerChanged;
    }

    public Date getLastClientChange() {
        return mLastClientChange;
    }

    public void setLastClientChange(Date lastClientChange) {
        mLastClientChange = lastClientChange;
    }
}
