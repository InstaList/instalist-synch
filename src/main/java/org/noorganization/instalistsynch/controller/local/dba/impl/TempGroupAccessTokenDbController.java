package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.noorganization.instalistsynch.controller.local.dba.ITempGroupAccessTokenDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.TempGroupAccessToken;

/**
 * SQLite implementation of {@link org.noorganization.instalistsynch.controller.local.dba.ITempGroupAccessTokenDbController}.
 * Created by tinos_000 on 08.02.2016.
 */
public class TempGroupAccessTokenDbController implements ITempGroupAccessTokenDbController {
    private static TempGroupAccessTokenDbController sInstance;
    private SynchDbHelper mDbHelper;

    /**
     * Get an instance of {@link GroupAuthDbController}.
     *
     * @param _context the context of the application.
     * @return the singleton object of this class.
     */
    public static TempGroupAccessTokenDbController getInstance(Context _context) {
        if (sInstance == null) {
            sInstance = new TempGroupAccessTokenDbController(_context);
        }
        return sInstance;
    }

    /**
     * Private Constructor that takes one argument.
     *
     * @param _context the context of the application.
     */
    private TempGroupAccessTokenDbController(Context _context) {
        mDbHelper = new SynchDbHelper(_context);
    }


    @Override
    public TempGroupAccessToken getAccessToken(int _groupId) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor accessCursor = db.query(TempGroupAccessToken.TABLE_NAME, TempGroupAccessToken.COLUMN.ALL_COLUMNS,
                TempGroupAccessToken.COLUMN.GROUP_ID + " = ? ",
                new String[]{String.valueOf(_groupId)}, null, null, null);

        if (accessCursor.getCount() == 0) {
            return null;
        }
        accessCursor.moveToFirst();
        int groupId = accessCursor.getInt(accessCursor.getColumnIndex(TempGroupAccessToken.COLUMN.GROUP_ID));
        String accessToken = accessCursor.getString(accessCursor.getColumnIndex(TempGroupAccessToken.COLUMN.TEMP_GROUP_ACCESS_TOKEN));
        boolean isLocal = accessCursor.getInt(accessCursor.getColumnIndex(TempGroupAccessToken.COLUMN.IS_LOCAL)) == 1;

        accessCursor.close();
        return new TempGroupAccessToken(groupId, accessToken, isLocal);
    }

    @Override
    public TempGroupAccessToken getLocalAccessToken() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor accessCursor = db.query(TempGroupAccessToken.TABLE_NAME, TempGroupAccessToken.COLUMN.ALL_COLUMNS,
                TempGroupAccessToken.COLUMN.IS_LOCAL + " = ? ",
                new String[]{String.valueOf(1)}, null, null, null);

        if (accessCursor.getCount() == 0) {
            return null;
        }
        accessCursor.moveToFirst();
        int groupId = accessCursor.getInt(accessCursor.getColumnIndex(TempGroupAccessToken.COLUMN.GROUP_ID));
        String accessToken = accessCursor.getString(accessCursor.getColumnIndex(TempGroupAccessToken.COLUMN.TEMP_GROUP_ACCESS_TOKEN));
        boolean isLocal = accessCursor.getInt(accessCursor.getColumnIndex(TempGroupAccessToken.COLUMN.IS_LOCAL)) == 1;
        accessCursor.close();
        return new TempGroupAccessToken(groupId, accessToken, isLocal);
    }

    @Override
    public boolean deleteAccessToken(int _groupId) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int deleted = db.delete(TempGroupAccessToken.TABLE_NAME,
                TempGroupAccessToken.COLUMN.GROUP_ID + " = ?",
                new String[]{String.valueOf(_groupId)});
        return deleted == 1;
    }

    @Override
    public boolean insertAccessToken(int _groupId, String _accessKey, boolean _isLocal) {
        // constraint because there should be only one local group
        if (_isLocal) {
            TempGroupAccessToken token = getLocalAccessToken();
            if (token != null)
                return false;
        }

        ContentValues cv = new ContentValues(3);
        cv.put(TempGroupAccessToken.COLUMN.GROUP_ID, _groupId);
        cv.put(TempGroupAccessToken.COLUMN.TEMP_GROUP_ACCESS_TOKEN, _accessKey);
        cv.put(TempGroupAccessToken.COLUMN.IS_LOCAL, _isLocal);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long insertionRow = db.insert(TempGroupAccessToken.TABLE_NAME, null, cv);

        return insertionRow >= 0;
    }
}
