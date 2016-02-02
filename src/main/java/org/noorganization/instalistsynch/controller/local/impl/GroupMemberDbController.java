package org.noorganization.instalistsynch.controller.local.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.noorganization.instalist.utils.SQLiteUtils;
import org.noorganization.instalistsynch.controller.local.IGroupMemberDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupMember;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of {@link IGroupMemberDbController} with a sqlite backend.
 * Created by tinos_000 on 02.02.2016.
 */
public class GroupMemberDbController implements IGroupMemberDbController {


    private static GroupMemberDbController sInstance;
    private SynchDbHelper mDbHelper;

    /**
     * Get an instance of {@link GroupMemberDbController}.
     *
     * @param _context the context of the application.
     * @return the singleton object of this class.
     */
    public static GroupMemberDbController getInstance(Context _context) {
        if (sInstance == null) {
            sInstance = new GroupMemberDbController(_context);
        }
        return sInstance;
    }

    /**
     * Private constructor.
     *
     * @param _context the context of the app.
     */
    private GroupMemberDbController(Context _context) {
        mDbHelper = new SynchDbHelper(_context);
    }

    @Override
    public GroupMember insert(GroupMember _groupMember) {
        if (isMemberInGroup(_groupMember))
            return null;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        String uuid = SQLiteUtils.generateId(db, GroupMember.TABLE_NAME).toString();

        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.ID, uuid);
        cv.put(GroupMember.COLUMN.DEVICE_ID, _groupMember.getDeviceId());
        cv.put(GroupMember.COLUMN.OWN_DEVICE_ID, _groupMember.getOwnDeviceId());
        cv.put(GroupMember.COLUMN.NAME, _groupMember.getName());
        cv.put(GroupMember.COLUMN.AUTHORIZED, _groupMember.isAuthorized());

        _groupMember.setUUID(uuid);
        long insertedRow = db.insert(GroupMember.TABLE_NAME, null, cv);
        return insertedRow >= 0L ? _groupMember : null;
    }


    @Override
    public boolean update(GroupMember _groupMember) {
        if (getById(_groupMember.getUUID()) == null)
            return false;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.ID, _groupMember.getUUID());
        cv.put(GroupMember.COLUMN.DEVICE_ID, _groupMember.getDeviceId());
        cv.put(GroupMember.COLUMN.OWN_DEVICE_ID, _groupMember.getOwnDeviceId());
        cv.put(GroupMember.COLUMN.NAME, _groupMember.getName());
        cv.put(GroupMember.COLUMN.AUTHORIZED, _groupMember.isAuthorized());

        int affectedRows = db.update(GroupMember.TABLE_NAME, cv, GroupMember.COLUMN.ID + " LIKE ?", new String[]{_groupMember.getUUID()});
        return affectedRows > 0;
    }

    @Override
    public boolean delete(String _uuid) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(GroupMember.TABLE_NAME, GroupMember.COLUMN.ID + " LIKE ?", new String[]{_uuid});
        return rowsDeleted > 0;
    }

    @Override
    public GroupMember getById(String _uuid) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.ID + "LIKE ?",
                new String[]{_uuid}, null, null, null);

        if (cursor.getCount() == 0)
            return null;

        cursor.moveToFirst();
        GroupMember groupMember = getGroupMemberModel(cursor);
        cursor.close();
        return groupMember;
    }

    @Override
    public List<GroupMember> getByOwnerId(String _ownId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.OWN_DEVICE_ID + "LIKE ?",
                new String[]{_ownId}, null, null, null);
        List<GroupMember> groupMemberList = new ArrayList<>();
        if (cursor.getCount() == 0)
            return null;

        cursor.moveToFirst();
        do {
            groupMemberList.add(getGroupMemberModel(cursor));
        } while (cursor.moveToNext());

        cursor.close();
        return groupMemberList;
    }

    /**
     * Checks if the given group member is already in group.
     *
     * @param _groupMember the group member to insert.
     * @return true if successful else false.
     */
    private boolean isMemberInGroup(GroupMember _groupMember) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.OWN_DEVICE_ID + "LIKE ? AND " + GroupMember.COLUMN.DEVICE_ID + "LIKE ?",
                new String[]{_groupMember.getOwnDeviceId(), _groupMember.getDeviceId()}, null, null, null);
        boolean ret = cursor.getCount() == 0;
        cursor.close();
        return ret;
    }

    /**
     * Extract a group member model from current cursor position
     *
     * @param _cursor the cursor to get the data from.
     * @return the groupmember.
     */
    private GroupMember getGroupMemberModel(Cursor _cursor) {
        GroupMember groupMember = new GroupMember();

        groupMember.setUUID(_cursor.getString(_cursor.getColumnIndex(GroupMember.COLUMN.ID)));
        groupMember.setAuthorized(_cursor.getInt(_cursor.getColumnIndex(GroupMember.COLUMN.AUTHORIZED)) == 1);
        groupMember.setDeviceId(_cursor.getString(_cursor.getColumnIndex(GroupMember.COLUMN.DEVICE_ID)));
        groupMember.setName(_cursor.getString(_cursor.getColumnIndex(GroupMember.COLUMN.NAME)));
        groupMember.setOwnDeviceId(_cursor.getString(_cursor.getColumnIndex(GroupMember.COLUMN.OWN_DEVICE_ID)));

        return groupMember;
    }
}
