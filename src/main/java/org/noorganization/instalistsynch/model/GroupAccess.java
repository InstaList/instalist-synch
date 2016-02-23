package org.noorganization.instalistsynch.model;

import org.noorganization.instalistsynch.utils.Constants;

import java.util.Date;

/**
 * Tracks the access to the groups.
 * Created by tinos_000 on 30.01.2016.
 */
public class GroupAccess {


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
        public static final String LAST_UPDATE_FROM_SERVER = "last_update_from_server";

        public static final String LAST_UPDATE_FROM_CLIENT = "last_update_from_client";
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
        public static final String ALL_COLUMNS[] = {GROUP_ID, TOKEN, LAST_UPDATE_FROM_SERVER, LAST_UPDATE_FROM_CLIENT, LAST_TOKEN_REQUEST, SYNCHRONIZE, INTERRUPTED};
    }

    public static final String DB_CREATE = "CREATE TABLE " + TABLE_NAME + " (" +
            COLUMN.GROUP_ID + " INTEGER PRIMARY KEY NOT NULL, " +
            COLUMN.TOKEN + " TEXT NOT NULL, " +
            COLUMN.LAST_UPDATE_FROM_SERVER + " TEXT NOT NULL, " +
            COLUMN.LAST_UPDATE_FROM_CLIENT + " TEXT NOT NULL, " +
            COLUMN.LAST_TOKEN_REQUEST + " TEXT NOT NULL," +
            COLUMN.SYNCHRONIZE + " INTEGER NOT NULL," +
            COLUMN.INTERRUPTED + " INTEGER NOT NULL," +
            "FOREIGN KEY (" + COLUMN.GROUP_ID + ") REFERENCES " + GroupAuth.TABLE_NAME + " (" +
            GroupAuth.COLUMN.GROUP_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
            ")";

    public GroupAccess(int groupId, String token) {
        mGroupId = groupId;
        mToken = token;
        mLastTokenRequest = new Date(Constants.INITIAL_DATE);
        mLastUpdateFromServer = new Date(Constants.INITIAL_DATE);
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
    private Date mLastUpdateFromServer;

    /**
     * The last update from the client.
     */
    private Date mLastUpdateFromClient;

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

    public Date getLastUpdateFromServer() {
        return mLastUpdateFromServer;
    }

    public void setLastUpdateFromServer(Date lastUpdateFromServer) {
        mLastUpdateFromServer = lastUpdateFromServer;
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

    public Date getLastUpdateFromClient() {
        return mLastUpdateFromClient;
    }

    public void setLastUpdateFromClient(Date lastUpdateFromClient) {
        mLastUpdateFromClient = lastUpdateFromClient;
    }
}
