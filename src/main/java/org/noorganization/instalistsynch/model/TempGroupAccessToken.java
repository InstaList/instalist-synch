package org.noorganization.instalistsynch.model;

/**
 * The temporary group access  token model. That includes the token and also the groupid.
 * Created by tinos_000 on 08.02.2016.
 */
public class TempGroupAccessToken {

    /**
     * Id of the group.
     */
    private int mGroupId;

    /**
     * Generated access token for the group.
     */
    private String mGroupAccessToken;

    public static final String TABLE_NAME = "temp_group_access_token";

    public static final class COLUMN {
        public final static String GROUP_ID = "group_id";
        public final static String TEMP_GROUP_ACCESS_TOKEN = "temp_group_access_token";

        public final static String[] ALL_COLUMNS = {GROUP_ID, TEMP_GROUP_ACCESS_TOKEN};

    }

    public static final String DB_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN.GROUP_ID + " INTEGER PRIMARY KEY NOT NULL," +
            COLUMN.TEMP_GROUP_ACCESS_TOKEN + " TEXT NOT NULL, " +
            "FOREIGN KEY (" + COLUMN.GROUP_ID + ") REFERENCES " + GroupAuth.TABLE_NAME + " ( " +
            GroupAuth.COLUMN.GROUP_ID + ") ON UPDATE CASCADE ON DELETE CASCADE " +
            ")";

    /**
     * Constructor of GroupAccessToken.
     *
     * @param _groupId          the groupID
     * @param _groupAccessToken the temporary generated groupAccess token.
     */
    public TempGroupAccessToken(int _groupId, String _groupAccessToken) {
        mGroupId = _groupId;
        mGroupAccessToken = _groupAccessToken;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    public String getGroupAccessToken() {
        return mGroupAccessToken;
    }

    public void setGroupAccessToken(String groupAccessToken) {
        mGroupAccessToken = groupAccessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TempGroupAccessToken)) return false;

        TempGroupAccessToken that = (TempGroupAccessToken) o;

        if (getGroupId() != that.getGroupId()) return false;
        return !(getGroupAccessToken() != null ? !getGroupAccessToken().equals(that.getGroupAccessToken()) : that.getGroupAccessToken() != null);

    }

    @Override
    public int hashCode() {
        int result = getGroupId();
        result = 31 * result + (getGroupAccessToken() != null ? getGroupAccessToken().hashCode() : 0);
        return result;
    }
}
