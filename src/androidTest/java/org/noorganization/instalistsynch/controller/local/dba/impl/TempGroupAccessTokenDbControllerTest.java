package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import org.noorganization.instalistsynch.controller.local.dba.ITempGroupAccessTokenDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.TempGroupAccessToken;

/**
 * Test for the groupAccesstokendbcontroller.
 * Created by tinos_000 on 08.02.2016.
 */
public class TempGroupAccessTokenDbControllerTest extends AndroidTestCase {

    private Context mContext;
    private SynchDbHelper mDbHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mDbHelper = new SynchDbHelper(mContext);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues(2);
        cv.put(TempGroupAccessToken.COLUMN.GROUP_ID, 1000000);
        cv.put(TempGroupAccessToken.COLUMN.TEMP_GROUP_ACCESS_TOKEN, "A983Fw3");

        long insertionRow = db.insert(TempGroupAccessToken.TABLE_NAME, null, cv);
        assertTrue(insertionRow >= 0);

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assertEquals(1, db.delete(TempGroupAccessToken.TABLE_NAME, TempGroupAccessToken.COLUMN.GROUP_ID + " = ? "
                , new String[]{String.valueOf(1000000)}));
        mContext = null;

    }

    public void testGetAccessToken() throws Exception {
        ITempGroupAccessTokenDbController tempGroupAccessTokenDbController = TempGroupAccessTokenDbController.getInstance(mContext);
        TempGroupAccessToken token = tempGroupAccessTokenDbController.getAccessToken(1000000);
        assertNotNull(token);
        assertEquals(1000000, token.getGroupId());
        assertEquals("A983Fw3", token.getGroupAccessToken());
    }

    public void testDeleteAccessToken() throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues(2);
        cv.put(TempGroupAccessToken.COLUMN.GROUP_ID, 1000001);
        cv.put(TempGroupAccessToken.COLUMN.TEMP_GROUP_ACCESS_TOKEN, "A983FB3");
        long insertionRow = db.insert(TempGroupAccessToken.TABLE_NAME, null, cv);
        assertTrue(insertionRow >= 0);

        ITempGroupAccessTokenDbController tempGroupAccessTokenDbController = TempGroupAccessTokenDbController.getInstance(mContext);
        assertTrue(tempGroupAccessTokenDbController.deleteAccessToken(1000001));

        Cursor query = db.query(TempGroupAccessToken.TABLE_NAME, TempGroupAccessToken.COLUMN.ALL_COLUMNS,
                TempGroupAccessToken.COLUMN.GROUP_ID + " = ? ",
                new String[]{String.valueOf(1000001)}, null, null, null);

        int count = query.getCount();
        query.close();
        assertEquals(0, count);
    }

    public void testInsertAccessToken() throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ITempGroupAccessTokenDbController tempGroupAccessTokenDbController = TempGroupAccessTokenDbController.getInstance(mContext);
        assertTrue(tempGroupAccessTokenDbController.insertAccessToken(1000002, "Trolaol"));

        Cursor query = db.query(TempGroupAccessToken.TABLE_NAME, TempGroupAccessToken.COLUMN.ALL_COLUMNS,
                TempGroupAccessToken.COLUMN.GROUP_ID + " = ? ",
                new String[]{String.valueOf(1000002)}, null, null, null);

        int count = query.getCount();
        query.close();

        assertEquals(1, count);
        // clean up
        assertEquals(1, db.delete(TempGroupAccessToken.TABLE_NAME, TempGroupAccessToken.COLUMN.GROUP_ID + " = ? "
                , new String[]{String.valueOf(1000002)}));

    }

    public void testInsertAccessTokenSameId() throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ITempGroupAccessTokenDbController tempGroupAccessTokenDbController = TempGroupAccessTokenDbController.getInstance(mContext);
        assertFalse(tempGroupAccessTokenDbController.insertAccessToken(1000000, "Trolaol"));


        Cursor query = db.query(TempGroupAccessToken.TABLE_NAME, TempGroupAccessToken.COLUMN.ALL_COLUMNS,
                TempGroupAccessToken.COLUMN.GROUP_ID + " = ? ",
                new String[]{String.valueOf(1000000)}, null, null, null);

        int count = query.getCount();
        if (count != 1)
            query.close();

        assertEquals(1, count);

        query.moveToFirst();
        String token = query.getString(query.getColumnIndex(TempGroupAccessToken.COLUMN.TEMP_GROUP_ACCESS_TOKEN));
        query.close();
        assertEquals("Trolaol", token);

    }
}