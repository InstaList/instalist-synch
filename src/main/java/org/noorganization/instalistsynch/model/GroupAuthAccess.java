package org.noorganization.instalistsynch.model;

import java.util.Date;

/**
 * Tracks the access to the groups.
 * Created by tinos_000 on 30.01.2016.
 */
public class GroupAuthAccess {


    public final static String TABLE_NAME = "groupauthaccess";

    /**
     * Holder of Column names.
     */
    public final static class COLUMN {
        /**
         * The column name of DeviceId.
         */
        public static final String GROUP_ID = "_id";

        /**
         * The column name of token.
         */
        public static final String TOKEN = "token";

        /**
         * The column name of last updated.
         */
        public static final String LAST_UPDATED = "last_updated";

        /**
         * The column name of last token request.
         */
        public static final String LAST_TOKEN_REQUEST = "last_token_request";

        /**
         * The column name of token.
         */
        public static final String SYNCHRONIZE = "synchronize";

        /**
         * The column name of interrupted.
         */
        public static final String INTERRUPTED = "interrupted";

        /**
         * All column names.
         */
        public static final String ALL_COLUMNS[] = {GROUP_ID, TOKEN, LAST_UPDATED, LAST_TOKEN_REQUEST, SYNCHRONIZE, INTERRUPTED};
    }

    public static final String DB_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN.GROUP_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            COLUMN.TOKEN + " TEXT NOT NULL, " +
            COLUMN.LAST_UPDATED + " TEXT NOT NULL, " +
            COLUMN.LAST_TOKEN_REQUEST + " TEXT NOT NULL," +
            COLUMN.SYNCHRONIZE + " INTEGER NOT NULL," +
            COLUMN.INTERRUPTED + " INTEGER NOT NULL," +
            "FOREIGN KEY (" + COLUMN.GROUP_ID + ") REFERENCES " + GroupAuth.TABLE_NAME + " (" +
            GroupAuth.COLUMN.GROUP_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
            ")";

    public GroupAuthAccess(int groupId, String token) {
        mGroupId = groupId;
        mToken = token;
        mLastTokenRequest = new Date(System.currentTimeMillis() - 100000000L);
        mLastUpdated = new Date(System.currentTimeMillis() - 100000000L);
    }


    /**
     * The generated deviceId.
     */
    private int mGroupId;

    /**
     * The auth token.
     */
    private String mToken;

    /**
     * The date when this device/group was last updated.
     */
    private Date mLastUpdated;

    /**
     * The date when the token was last refreshed.
     */
    private Date mLastTokenRequest;

    /**
     * Indicates if the group should be synchronized.
     */
    private boolean mSynchronize;

    /**
     * Indicates if the last run was interrupted.
     */
    private boolean mInterrupted;

    public int getGroupId() {
        return mGroupId;
    }

    public String getToken() {
        return mToken;
    }

    public void setToken(String token) {
        mToken = token;
    }

    public Date getLastUpdated() {
        return mLastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        mLastUpdated = lastUpdated;
    }

    public Date getLastTokenRequest() {
        return mLastTokenRequest;
    }

    public void setLastTokenRequest(Date lastTokenRequest) {
        mLastTokenRequest = lastTokenRequest;
    }

    public boolean isSynchronize() {
        return mSynchronize;
    }

    public void setSynchronize(boolean synchronize) {
        mSynchronize = synchronize;
    }

    public boolean wasInterrupted() {
        return mInterrupted;
    }

    public void setInterrupted(boolean interrupted) {
        mInterrupted = interrupted;
    }
}
