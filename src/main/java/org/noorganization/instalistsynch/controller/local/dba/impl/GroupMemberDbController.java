package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import org.noorganization.instalistsynch.controller.local.dba.IGroupMemberDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.AccessRight;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.eSortMode;

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
    public GroupMember insert(@NonNull GroupMember _groupMember) {
        if (_groupMember.hasNullFields())
            return null;
        if (isMemberInGroup(_groupMember))
            return null;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        AccessRight accessRights = _groupMember.getAccessRights();

        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.GROUP_ID, _groupMember.getGroupId());
        cv.put(GroupMember.COLUMN.DEVICE_ID, _groupMember.getDeviceId());
        cv.put(GroupMember.COLUMN.NAME, _groupMember.getName());
        cv.put(GroupMember.COLUMN.AUTHORIZED, accessRights.hasReadRight() && accessRights.hasWriteRight() ? 1 : 0);
        long insertedRow = db.insert(GroupMember.TABLE_NAME, null, cv);
        return insertedRow >= 0L ? _groupMember : null;
    }


    @Override
    public boolean update(@NonNull GroupMember _groupMember) {
        if (_groupMember.hasNullFields())
            return false;
        if (getById(_groupMember.getGroupId(), _groupMember.getDeviceId()) == null)
            return false;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        AccessRight accessRights = _groupMember.getAccessRights();

        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.GROUP_ID, _groupMember.getGroupId());
        cv.put(GroupMember.COLUMN.DEVICE_ID, _groupMember.getDeviceId());
        cv.put(GroupMember.COLUMN.NAME, _groupMember.getName());
        cv.put(GroupMember.COLUMN.AUTHORIZED, accessRights.hasReadRight() && accessRights.hasWriteRight() ? 1 : 0);

        int affectedRows = db.update(GroupMember.TABLE_NAME, cv,
                GroupMember.COLUMN.GROUP_ID + " = ? AND " + GroupMember.COLUMN.DEVICE_ID + " = ? ",
                new String[]{String.valueOf(_groupMember.getGroupId()), String.valueOf(_groupMember.getDeviceId())});
        return affectedRows > 0;
    }

    @Override
    public boolean delete(int _groupId, int _deviceId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int rowsDeleted = db.delete(GroupMember.TABLE_NAME, GroupMember.COLUMN.GROUP_ID + " = ? AND " + GroupMember.COLUMN.DEVICE_ID + " = ? ",
                new String[]{String.valueOf(_groupId), String.valueOf(_deviceId)});
        return rowsDeleted > 0;
    }

    @Override
    public GroupMember getById(int _groupId, int _deviceId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ? AND " + GroupMember.COLUMN.DEVICE_ID + " = ? ",
                new String[]{String.valueOf(_groupId), String.valueOf(_deviceId)}, null, null, null);

        if (cursor.getCount() == 0)
            return null;

        cursor.moveToFirst();
        GroupMember groupMember = getGroupMemberModel(cursor);
        cursor.close();
        return groupMember;
    }

    @Override
    public List<GroupMember> getByGroup(int _groupId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + "= ?",
                new String[]{String.valueOf(_groupId)}, null, null, null);
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

    @Override
    public Cursor getCursorByGroup(int _groupId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + "= ?",
                new String[]{String.valueOf(_groupId)}, null, null, null);
        return cursor;
    }

    @Override
    public Cursor getAllGroupMembersByGroup(eSortMode _sortMode) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, null,
                null, null, null, GroupMember.COLUMN.GROUP_ID + " " + _sortMode.toString());
        return cursor;
    }

    /**
     * Checks if the given group member is already in group.
     *
     * @param _groupMember the group member to insert.
     * @return true if successful else false.
     */
    private boolean isMemberInGroup(@NonNull GroupMember _groupMember) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ? AND " + GroupMember.COLUMN.DEVICE_ID + " = ? ",
                new String[]{String.valueOf(_groupMember.getGroupId()), String.valueOf(_groupMember.getDeviceId())}, null, null, null);
        boolean ret = cursor.getCount() == 1;
        cursor.close();
        return ret;
    }

    /**
     * Extract a group member model from current cursor position
     *
     * @param _cursor the cursor to get the data from.
     * @return the groupmember.
     */
    private GroupMember getGroupMemberModel(@NonNull Cursor _cursor) {
        GroupMember groupMember = new GroupMember();

        boolean access = _cursor.getInt(_cursor.getColumnIndex(GroupMember.COLUMN.AUTHORIZED)) == 1;
        // just a placeholder for later usage. true for both rights, member is authorized. false for both if not.
        AccessRight accessRights = access ? new AccessRight(true, true) : new AccessRight(false, false);

        groupMember.setGroupId(_cursor.getInt(_cursor.getColumnIndex(GroupMember.COLUMN.GROUP_ID)));
        groupMember.setAccessRights(accessRights);
        groupMember.setDeviceId(_cursor.getInt(_cursor.getColumnIndex(GroupMember.COLUMN.DEVICE_ID)));
        groupMember.setName(_cursor.getString(_cursor.getColumnIndex(GroupMember.COLUMN.NAME)));

        return groupMember;
    }
}
