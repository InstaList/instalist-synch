package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.codehaus.jackson.map.util.ISO8601Utils;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.eSortMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The implementation of {@link IGroupAuthAccessDbController} that manages the access to the sqlite database.
 * Created by tinos_000 on 30.01.2016.
 */
public class SqliteGroupAuthAccessDbController implements IGroupAuthAccessDbController {

    private static SqliteGroupAuthAccessDbController sInstance;
    private SynchDbHelper mDbHelper;

    /**
     * Get an instance of {@link GroupAuthDbController}.
     *
     * @param _context the context of the application.
     * @return the singleton object of this class.
     */
    public static SqliteGroupAuthAccessDbController getInstance(Context _context) {
        if (sInstance == null) {
            sInstance = new SqliteGroupAuthAccessDbController(_context);
        }
        return sInstance;
    }

    /**
     * Private constructor.
     *
     * @param _context the context of the app.
     */
    private SqliteGroupAuthAccessDbController(Context _context) {
        mDbHelper = new SynchDbHelper(_context);
    }

    @Override
    public int insert(GroupAuthAccess _groupAuthAccess) {
        if (hasIdInDatabase(_groupAuthAccess.getGroupId())) {
            return INSERTION_CODE.ELEMENT_EXISTS;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues(4);
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, _groupAuthAccess.getGroupId());
        cv.put(GroupAuthAccess.COLUMN.TOKEN, _groupAuthAccess.getToken() == null? "" : _groupAuthAccess.getToken());
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(_groupAuthAccess.getLastUpdateFromServer()));
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_CLIENT, ISO8601Utils.format(_groupAuthAccess.getLastUpdateFromClient()));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(_groupAuthAccess.getLastTokenRequest()));
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, _groupAuthAccess.isSynchronize());
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, _groupAuthAccess.wasInterrupted());

        long rowId = db.insert(GroupAuthAccess.TABLE_NAME, null, cv);
        if (rowId == -1)
            return INSERTION_CODE.ERROR;

        return INSERTION_CODE.CORRECT;
    }

    @Override
    public GroupAuthAccess getGroupAuthAccess(int _groupId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAuthAccess.TABLE_NAME, GroupAuthAccess.COLUMN.ALL_COLUMNS, GroupAuthAccess.COLUMN.GROUP_ID + " = ?", new String[]{String.valueOf(_groupId)}, null, null, null);
        // check if there is an element.
        List<GroupAuthAccess> groupAuthAccessList = getList(authAccessCursor);
        authAccessCursor.close();

        if (groupAuthAccessList.isEmpty())
            return null;

        return groupAuthAccessList.get(0);
    }

    @Override
    public List<GroupAuthAccess> getGroupAuthAccesses() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAuthAccess.TABLE_NAME, GroupAuthAccess.COLUMN.ALL_COLUMNS, null, null, null, null, null);
        List<GroupAuthAccess> groupAuthAccessList = getList(authAccessCursor);
        authAccessCursor.close();
        return groupAuthAccessList;
    }

    @Override
    public Cursor getGroupAuthAccessesCursor(eSortMode _sortMode) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAuthAccess.TABLE_NAME, GroupAuthAccess.COLUMN.ALL_COLUMNS, null, null, null, null, GroupAuthAccess.COLUMN.GROUP_ID + " " + _sortMode.toString());
        return authAccessCursor;
    }

    @Override
    public List<GroupAuthAccess> getGroupAuthAccesses(String _sinceTime) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAuthAccess.TABLE_NAME,
                GroupAuthAccess.COLUMN.ALL_COLUMNS,
                "datetime(" + GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER + ") >= datetime(?)",
                new String[]{_sinceTime}, null, null, null);

        List<GroupAuthAccess> groupAuthAccessList = getList(authAccessCursor);
        authAccessCursor.close();
        return groupAuthAccessList;
    }

    @Override
    public List<GroupAuthAccess> getGroupAuthAccesses(boolean _synchronize) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAuthAccess.TABLE_NAME,
                GroupAuthAccess.COLUMN.ALL_COLUMNS,
                GroupAuthAccess.COLUMN.SYNCHRONIZE + "= ? ",
                new String[]{String.valueOf(_synchronize ? 1 : 0)},
                null, null, null);
        List<GroupAuthAccess> groupAuthAccessList = getList(authAccessCursor);
        authAccessCursor.close();
        return groupAuthAccessList;
    }


    @Override
    public boolean update(GroupAuthAccess _groupAuthAccess) {
        if (!hasIdInDatabase(_groupAuthAccess.getGroupId()))
            return false;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues(4);
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, _groupAuthAccess.getGroupId());
        cv.put(GroupAuthAccess.COLUMN.TOKEN, _groupAuthAccess.getToken());
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(_groupAuthAccess.getLastUpdateFromServer()));
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_CLIENT, ISO8601Utils.format(_groupAuthAccess.getLastUpdateFromClient()));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(_groupAuthAccess.getLastTokenRequest()));
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, _groupAuthAccess.isSynchronize());
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, _groupAuthAccess.wasInterrupted());

        long updatedRows = db.update(GroupAuthAccess.TABLE_NAME, cv, GroupAuthAccess.COLUMN.GROUP_ID + " = ?", new String[]{String.valueOf(_groupAuthAccess.getGroupId())});
        if (updatedRows <= 0)
            return false;
        return true;
    }


    @Override
    public boolean updateToken(int _groupId, String _newToken) {
        if (!hasIdInDatabase(_groupId))
            return false;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        GroupAuthAccess groupAuthAccess = getGroupAuthAccess(_groupId);
        groupAuthAccess.setLastTokenRequest(new Date());
        groupAuthAccess.setToken(_newToken);

        ContentValues cv = new ContentValues(6);
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, groupAuthAccess.getGroupId());
        cv.put(GroupAuthAccess.COLUMN.TOKEN, groupAuthAccess.getToken());
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(groupAuthAccess.getLastUpdateFromServer()));
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_CLIENT, ISO8601Utils.format(groupAuthAccess.getLastUpdateFromClient()));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(groupAuthAccess.getLastTokenRequest()));
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, groupAuthAccess.isSynchronize());
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, groupAuthAccess.wasInterrupted());

        long updatedRows = db.update(GroupAuthAccess.TABLE_NAME, cv, GroupAuthAccess.COLUMN.GROUP_ID + " = ?", new String[]{String.valueOf(_groupId)});
        return updatedRows > 0;
    }

    @Override
    public boolean hasIdInDatabase(int _groupId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAuthAccess.TABLE_NAME, GroupAuthAccess.COLUMN.ALL_COLUMNS, GroupAuthAccess.COLUMN.GROUP_ID + " = ?", new String[]{String.valueOf(_groupId)}, null, null, null);
        boolean ret = authAccessCursor.getCount() == 1;
        authAccessCursor.close();
        return ret;
    }


    /**
     * Fetches all element from cursor and creates a List of GroupAuthAccess elements.
     * !important it dont close the given cursor, please close it after calling.
     *
     * @param _cursor the cursor to the {@link GroupAuthAccess} elements.
     * @return the objects in lists given by cursor.
     */
    private List<GroupAuthAccess> getList(Cursor _cursor) {
        List<GroupAuthAccess> groupAuthAccesses = new ArrayList<>(_cursor.getCount());
        if (!_cursor.moveToFirst()) {
            return groupAuthAccesses;
        }

        do {
            int groupId = _cursor.getInt(_cursor.getColumnIndex(GroupAuthAccess.COLUMN.GROUP_ID));
            String token = _cursor.getString(_cursor.getColumnIndex(GroupAuthAccess.COLUMN.TOKEN));
            String lastUpdateFromServer = _cursor.getString(_cursor.getColumnIndex(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER));
            String lastUpdateFromClient = _cursor.getString(_cursor.getColumnIndex(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_CLIENT));
            String lastTokenRequest = _cursor.getString(_cursor.getColumnIndex(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST));
            boolean interrupted = _cursor.getInt(_cursor.getColumnIndex(GroupAuthAccess.COLUMN.INTERRUPTED)) == 1;
            boolean synchronize = _cursor.getInt(_cursor.getColumnIndex(GroupAuthAccess.COLUMN.SYNCHRONIZE)) == 1;

            GroupAuthAccess groupAuthAccess = new GroupAuthAccess(groupId, token);
            groupAuthAccess.setLastTokenRequest(ISO8601Utils.parse(lastTokenRequest));
            groupAuthAccess.setLastUpdateFromServer(ISO8601Utils.parse(lastUpdateFromServer));
            groupAuthAccess.setLastUpdateFromClient(ISO8601Utils.parse(lastUpdateFromClient));
            groupAuthAccess.setInterrupted(interrupted);
            groupAuthAccess.setSynchronize(synchronize);
            groupAuthAccesses.add(groupAuthAccess);
        } while (_cursor.moveToNext());

        return groupAuthAccesses;
    }
}
