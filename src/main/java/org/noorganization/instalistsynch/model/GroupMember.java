package org.noorganization.instalistsynch.model;

/**
 * Represents a member of a group.
 * Created by Desnoo on 01.02.2016.
 */
public class GroupMember {

    /**
     * The id of the group.
     */
    private int mGroupId;

    /**
     * The device id of each device.
     */
    private int mDeviceId;

    /**
     * The name of the group member.
     */
    private String mName;

    /**
     * Indicates which rights the user has.
     */
    private AccessRight mAccessRights;


    public final static class COLUMN {
        public static final String GROUP_ID = "group_id";
        public static final String DEVICE_ID = "device_id";
        public static final String NAME = "name";
        public static final String AUTHORIZED = "authorized";
        public static final String ALL_COLUMNS[] = {GROUP_ID, DEVICE_ID, NAME, AUTHORIZED};
    }

    public static final String TABLE_NAME = "group_member";

    public static String DB_CREATE = "CREATE TABLE " + TABLE_NAME + "(" +
            COLUMN.GROUP_ID + " INTEGER NOT NULL, " +
            COLUMN.DEVICE_ID + " INTEGER NOT NULL, " +
            COLUMN.AUTHORIZED + " INTEGER NOT NULL, " +
            COLUMN.NAME + " TEXT NOT NULL, " +
            "FOREIGN KEY (" + COLUMN.GROUP_ID + ") REFERENCES " + GroupAuth.TABLE_NAME + " ( " +
            GroupAuth.COLUMN.GROUP_ID + ") ON UPDATE CASCADE ON DELETE CASCADE, " +
            "PRIMARY KEY (" + COLUMN.GROUP_ID + "," + COLUMN.DEVICE_ID + ")" +
            ")";

    public GroupMember() {
    }

    /**
     * Constructor of GroupMember object.
     *
     * @param _groupId    the groupId.
     * @param _deviceId   the id of the device.
     * @param _name       the name of the device in the group.
     * @param _authorized true if the group was already authorized.
     */
    public GroupMember(int _groupId, int _deviceId, String _name, AccessRight _authorized) {
        mGroupId = _groupId;
        mDeviceId = _deviceId;
        mName = _name;
        mAccessRights = _authorized;
    }


    public String getName() {
        return mName;
    }

    public void setName(String _name) {
        mName = _name;
    }

    public AccessRight getAccessRights() {
        return mAccessRights;
    }

    public void setAccessRights(AccessRight _accessRights) {
        mAccessRights = _accessRights;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int groupId) {
        mGroupId = groupId;
    }

    public int getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(int deviceId) {
        mDeviceId = deviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupMember)) return false;

        GroupMember that = (GroupMember) o;

        if (getGroupId() != that.getGroupId()) return false;
        if (getDeviceId() != that.getDeviceId()) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null)
            return false;
        return !(getAccessRights() != null ? !getAccessRights().equals(that.getAccessRights()) : that.getAccessRights() != null);

    }

    @Override
    public int hashCode() {
        int result = getGroupId();
        result = 31 * result + getDeviceId();
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getAccessRights() != null ? getAccessRights().hashCode() : 0);
        return result;
    }

    /**
     * Checks if the object has some null fields.
     * @return true if it has, else false.
     */
    public boolean hasNullFields(){
        return this.mAccessRights == null || this.mName == null;
    }
}
