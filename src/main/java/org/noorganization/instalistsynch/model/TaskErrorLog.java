package org.noorganization.instalistsynch.model;

/**
 * log where all errors are saved.
 * Created by Desnoo on 15.02.2016.
 */
public class TaskErrorLog {

    private int mId;
    /**
     * Server side uuid.
     */
    private String mUUID;
    private int mGroupId;
    private int mType;
    private int mErrorType;

    public TaskErrorLog(int id, String UUID, int groupId, int type, int errorType) {
        mId = id;
        mUUID = UUID;
        mGroupId = groupId;
        mType = type;
        mErrorType = errorType;
    }

    public final static class COLUMN {
        public final static String ID = "_id";
        public final static String SERVER_UUID = "server_uuid";
        public final static String GROUP_ID = "group_id";
        public final static String TYPE = "type";
        public final static String ERROR_TYPE = "error_type";

        public final static String ALL_COLUMNS[] = {ID, SERVER_UUID, GROUP_ID, TYPE, ERROR_TYPE};
    }

    public final static String TABLE_NAME = "task_error_log";

    public final static String DB_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN.ID + " INTEGER PRIMARY KEY NOT NULL," +
            COLUMN.GROUP_ID + " INTEGER NOT NULL, " +
            COLUMN.SERVER_UUID + " TEXT NOT NULL," +
            COLUMN.TYPE + " INTEGER NOT NULL," +
            COLUMN.ERROR_TYPE + " INTEGER NOT NULL, " +
            " FOREIGN KEY (" + COLUMN.GROUP_ID + ") REFERENCES " + GroupAuth.TABLE_NAME + " (" +
            GroupAuth.COLUMN.GROUP_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
            ")";


    public String getUUID() {
        return mUUID;
    }

    public void setUUID(String UUID) {
        mUUID = UUID;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public int getErrorType() {
        return mErrorType;
    }

    public void setErrorType(int errorType) {
        mErrorType = errorType;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }
}
