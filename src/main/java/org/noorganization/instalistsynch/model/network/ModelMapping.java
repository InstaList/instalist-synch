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

}
