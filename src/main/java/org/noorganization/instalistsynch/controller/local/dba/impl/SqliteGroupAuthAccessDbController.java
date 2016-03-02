/*
 * Copyright 2016 Tino Siegmund, Michael Wodniok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.codehaus.jackson.map.util.ISO8601Utils;
import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.model.eSortMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

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
    public int insert(GroupAccess _groupAccess) {
        if (hasIdInDatabase(_groupAccess.getGroupId())) {
            return INSERTION_CODE.ELEMENT_EXISTS;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues(4);
        cv.put(GroupAccess.COLUMN.GROUP_ID, _groupAccess.getGroupId());
        cv.put(GroupAccess.COLUMN.TOKEN, _groupAccess.getToken() == null? "" : _groupAccess.getToken());
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(_groupAccess.getLastUpdateFromServer(),false, TimeZone.getTimeZone("GMT+0000")));
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_CLIENT, ISO8601Utils.format(_groupAccess.getLastUpdateFromClient(),false, TimeZone.getTimeZone("GMT+0000")));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(_groupAccess.getLastTokenRequest(), false, TimeZone.getTimeZone("GMT+0000")));
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, _groupAccess.isSynchronize());
        cv.put(GroupAccess.COLUMN.INTERRUPTED, _groupAccess.wasInterrupted());

        long rowId = db.insert(GroupAccess.TABLE_NAME, null, cv);
        if (rowId == -1)
            return INSERTION_CODE.ERROR;

        return INSERTION_CODE.CORRECT;
    }

    @Override
    public GroupAccess getGroupAuthAccess(int _groupId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAccess.TABLE_NAME, GroupAccess.COLUMN.ALL_COLUMNS, GroupAccess.COLUMN.GROUP_ID + " = ?", new String[]{String.valueOf(_groupId)}, null, null, null);
        // check if there is an element.
        List<GroupAccess> groupAccessList = getList(authAccessCursor);
        authAccessCursor.close();

        if (groupAccessList.isEmpty())
            return null;

        return groupAccessList.get(0);
    }

    @Override
    public List<GroupAccess> getGroupAuthAccesses() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAccess.TABLE_NAME, GroupAccess.COLUMN.ALL_COLUMNS, null, null, null, null, null);
        List<GroupAccess>
                groupAccessList = getList(authAccessCursor);
        authAccessCursor.close();
        return groupAccessList;
    }

    @Override
    public Cursor getGroupAuthAccessesCursor(eSortMode _sortMode) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAccess.TABLE_NAME, GroupAccess.COLUMN.ALL_COLUMNS, null, null, null, null, GroupAccess.COLUMN.GROUP_ID + " " + _sortMode.toString());
        return authAccessCursor;
    }

    @Override
    public List<GroupAccess> getGroupAuthAccesses(String _sinceTime) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAccess.TABLE_NAME,
                GroupAccess.COLUMN.ALL_COLUMNS,
                "" + GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER + " >= ?",
                new String[]{_sinceTime}, null, null, null);

        List<GroupAccess> groupAccessList = getList(authAccessCursor);
        authAccessCursor.close();
        return groupAccessList;
    }

    @Override
    public List<GroupAccess> getGroupAuthAccesses(boolean _synchronize) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAccess.TABLE_NAME,
                GroupAccess.COLUMN.ALL_COLUMNS,
                GroupAccess.COLUMN.SYNCHRONIZE + "= ? ",
                new String[]{String.valueOf(_synchronize ? 1 : 0)},
                null, null, null);
        List<GroupAccess> groupAccessList = getList(authAccessCursor);
        authAccessCursor.close();
        return groupAccessList;
    }


    @Override
    public boolean update(GroupAccess _groupAccess) {
        if (!hasIdInDatabase(_groupAccess.getGroupId()))
            return false;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues(4);
        cv.put(GroupAccess.COLUMN.GROUP_ID, _groupAccess.getGroupId());
        cv.put(GroupAccess.COLUMN.TOKEN, _groupAccess.getToken());
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(_groupAccess.getLastUpdateFromServer(),false, TimeZone.getTimeZone("GMT+0000")));
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_CLIENT, ISO8601Utils.format(_groupAccess.getLastUpdateFromClient(),false, TimeZone.getTimeZone("GMT+0000")));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(_groupAccess.getLastTokenRequest(),false, TimeZone.getTimeZone("GMT+0000")));
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, _groupAccess.isSynchronize());
        cv.put(GroupAccess.COLUMN.INTERRUPTED, _groupAccess.wasInterrupted());

        long updatedRows = db.update(GroupAccess.TABLE_NAME, cv, GroupAccess.COLUMN.GROUP_ID + " = ?", new String[]{String.valueOf(
                _groupAccess.getGroupId())});
        if (updatedRows <= 0)
            return false;
        return true;
    }


    @Override
    public boolean updateToken(int _groupId, String _newToken) {
        if (!hasIdInDatabase(_groupId))
            return false;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        GroupAccess groupAccess = getGroupAuthAccess(_groupId);
        groupAccess.setLastTokenRequest(new Date());
        groupAccess.setToken(_newToken);

        ContentValues cv = new ContentValues(6);
        cv.put(GroupAccess.COLUMN.GROUP_ID, groupAccess.getGroupId());
        cv.put(GroupAccess.COLUMN.TOKEN, groupAccess.getToken());
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(groupAccess.getLastUpdateFromServer(),false, TimeZone.getTimeZone("GMT+0000")));
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_CLIENT, ISO8601Utils.format(groupAccess.getLastUpdateFromClient(),false, TimeZone.getTimeZone("GMT+0000")));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(groupAccess.getLastTokenRequest(),false, TimeZone.getTimeZone("GMT+0000")));
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, groupAccess.isSynchronize());
        cv.put(GroupAccess.COLUMN.INTERRUPTED, groupAccess.wasInterrupted());

        long updatedRows = db.update(GroupAccess.TABLE_NAME, cv, GroupAccess.COLUMN.GROUP_ID + " = ?", new String[]{String.valueOf(_groupId)});
        return updatedRows > 0;
    }

    @Override
    public boolean hasIdInDatabase(int _groupId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authAccessCursor = db.query(GroupAccess.TABLE_NAME, GroupAccess.COLUMN.ALL_COLUMNS, GroupAccess.COLUMN.GROUP_ID + " = ?", new String[]{String.valueOf(_groupId)}, null, null, null);
        boolean ret = authAccessCursor.getCount() == 1;
        authAccessCursor.close();
        return ret;
    }


    /**
     * Fetches all element from cursor and creates a List of GroupAccess elements.
     * !important it dont close the given cursor, please close it after calling.
     *
     * @param _cursor the cursor to the {@link GroupAccess} elements.
     * @return the objects in lists given by cursor.
     */
    private List<GroupAccess> getList(Cursor _cursor) {
        List<GroupAccess> groupAccesses = new ArrayList<>(_cursor.getCount());
        if (!_cursor.moveToFirst()) {
            return groupAccesses;
        }

        do {
            int groupId = _cursor.getInt(_cursor.getColumnIndex(GroupAccess.COLUMN.GROUP_ID));
            String token = _cursor.getString(_cursor.getColumnIndex(GroupAccess.COLUMN.TOKEN));
            String lastUpdateFromServer = _cursor.getString(_cursor.getColumnIndex(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER));
            String lastUpdateFromClient = _cursor.getString(_cursor.getColumnIndex(GroupAccess.COLUMN.LAST_UPDATE_FROM_CLIENT));
            String lastTokenRequest = _cursor.getString(_cursor.getColumnIndex(GroupAccess.COLUMN.LAST_TOKEN_REQUEST));
            boolean interrupted = _cursor.getInt(_cursor.getColumnIndex(GroupAccess.COLUMN.INTERRUPTED)) == 1;
            boolean synchronize = _cursor.getInt(_cursor.getColumnIndex(GroupAccess.COLUMN.SYNCHRONIZE)) == 1;

            GroupAccess groupAccess = new GroupAccess(groupId, token);
            groupAccess.setLastTokenRequest(ISO8601Utils.parse(lastTokenRequest));
            groupAccess.setLastUpdateFromServer(ISO8601Utils.parse(lastUpdateFromServer));
            groupAccess.setLastUpdateFromClient(ISO8601Utils.parse(lastUpdateFromClient));
            groupAccess.setInterrupted(interrupted);
            groupAccess.setSynchronize(synchronize);
            groupAccesses.add(groupAccess);
        } while (_cursor.moveToNext());

        return groupAccesses;
    }
}
