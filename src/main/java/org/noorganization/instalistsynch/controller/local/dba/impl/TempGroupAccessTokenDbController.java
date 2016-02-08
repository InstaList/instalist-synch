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
                TempGroupAccessToken.COLUMN.GROUP_ID + " = ?",
                new String[]{String.valueOf(_groupId)}, null, null, null);

        if (accessCursor.getCount() == 0) {
            return null;
        }
        accessCursor.moveToFirst();
        String accessToken = accessCursor.getString(accessCursor.getColumnIndex(TempGroupAccessToken.COLUMN.TEMP_GROUP_ACCESS_TOKEN));
        accessCursor.close();
        return new TempGroupAccessToken(_groupId, accessToken);
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
    public boolean insertAccessToken(int _groupId, String _accessKey) {
        ContentValues cv = new ContentValues(2);
        cv.put(TempGroupAccessToken.COLUMN.GROUP_ID, _groupId);
        cv.put(TempGroupAccessToken.COLUMN.TEMP_GROUP_ACCESS_TOKEN, _accessKey);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        long insertionRow = db.insert(TempGroupAccessToken.TABLE_NAME, null, cv);

        return insertionRow >= 0;
    }
}
