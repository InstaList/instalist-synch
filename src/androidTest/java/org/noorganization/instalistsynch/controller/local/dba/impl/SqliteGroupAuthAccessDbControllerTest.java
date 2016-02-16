package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupAuthAccess;

import java.text.ParsePosition;
import java.util.Date;
import java.util.List;

/**
 * Tests for the sqliteGroupAuthAccessDB Controller
 * Created by tinos_000 on 09.02.2016.
 */
public class SqliteGroupAuthAccessDbControllerTest extends AndroidTestCase {

    private Context mContext;
    private SynchDbHelper mDbHelper;
    private IGroupAuthAccessDbController mGroupAuthAccessDbController;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mDbHelper = new SynchDbHelper(mContext);
        mGroupAuthAccessDbController = SqliteGroupAuthAccessDbController.getInstance(mContext);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mDbHelper.onUpgrade(mDbHelper.getReadableDatabase(), 1, 1);
    }

    public void testInsert() throws Exception {
        Date currentDate = new Date();
        GroupAuthAccess groupAuthAccess = new GroupAuthAccess(1, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");
        groupAuthAccess.setLastTokenRequest(currentDate);
        groupAuthAccess.setLastUpdateFromServer(currentDate);
        groupAuthAccess.setSynchronize(true);
        groupAuthAccess.setInterrupted(false);

        assertEquals(IGroupAuthAccessDbController.INSERTION_CODE.CORRECT, mGroupAuthAccessDbController.insert(groupAuthAccess));

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupAuthAccess.TABLE_NAME, GroupAuthAccess.COLUMN.ALL_COLUMNS, GroupAuthAccess.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0)
            cursor.close();

        assertTrue(cursor.moveToFirst());

        int groupId = cursor.getInt(cursor.getColumnIndex(GroupAuthAccess.COLUMN.GROUP_ID));
        boolean synchronize = cursor.getInt(cursor.getColumnIndex(GroupAuthAccess.COLUMN.SYNCHRONIZE)) == 1;
        boolean interrupted = cursor.getInt(cursor.getColumnIndex(GroupAuthAccess.COLUMN.INTERRUPTED)) == 1;
        Date lastTokenRequestDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST)), new ParsePosition(0));
        Date lastUpdateDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER)), new ParsePosition(0));
        String token = cursor.getString(cursor.getColumnIndex(GroupAuthAccess.COLUMN.TOKEN));

        cursor.close();

        assertEquals(1, groupId);
        assertEquals(true, synchronize);
        assertEquals(false, interrupted);
        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(lastTokenRequestDate));
        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(lastUpdateDate));
        assertEquals("fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.", token);

    }

    public void testGetGroupAuthAccess() throws Exception {
        Date currentDate = new Date();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        GroupAuthAccess groupAuthAccess = mGroupAuthAccessDbController.getGroupAuthAccess(1);
        assertNotNull(groupAuthAccess);
        assertEquals(1, groupAuthAccess.getGroupId());
        assertEquals(false, groupAuthAccess.wasInterrupted());
        assertEquals(true, groupAuthAccess.isSynchronize());
        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAuthAccess.getLastUpdateFromServer()));
        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAuthAccess.getLastTokenRequest()));
        assertEquals("fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.", groupAuthAccess.getToken());
    }

    public void testInsertSameId() throws Exception {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Date currentDate = new Date();
        GroupAuthAccess groupAuthAccess = new GroupAuthAccess(1, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");
        groupAuthAccess.setLastTokenRequest(currentDate);
        groupAuthAccess.setLastUpdateFromServer(currentDate);
        groupAuthAccess.setSynchronize(true);
        groupAuthAccess.setInterrupted(false);

        assertEquals(IGroupAuthAccessDbController.INSERTION_CODE.CORRECT, mGroupAuthAccessDbController.insert(groupAuthAccess));

        groupAuthAccess = new GroupAuthAccess(1, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");
        groupAuthAccess.setLastTokenRequest(currentDate);
        groupAuthAccess.setLastUpdateFromServer(currentDate);
        groupAuthAccess.setSynchronize(true);
        groupAuthAccess.setInterrupted(false);

        assertEquals(IGroupAuthAccessDbController.INSERTION_CODE.ELEMENT_EXISTS, mGroupAuthAccessDbController.insert(groupAuthAccess));
    }

    public void testGetGroupAuthAccesses() throws Exception {

        Date currentDate = new Date();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        Date otherDate = new Date();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 2);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, true);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(otherDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(otherDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        List<GroupAuthAccess> groupAuthAccessList = mGroupAuthAccessDbController.getGroupAuthAccesses();
        assertNotNull(groupAuthAccessList);
        assertEquals(2, groupAuthAccessList.size());

        for (GroupAuthAccess groupAuthAccess : groupAuthAccessList) {
            assertNotNull(groupAuthAccess);
            switch (groupAuthAccess.getGroupId()) {
                case 1:
                    assertEquals(1, groupAuthAccess.getGroupId());
                    assertEquals(false, groupAuthAccess.wasInterrupted());
                    assertEquals(true, groupAuthAccess.isSynchronize());
                    assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAuthAccess.getLastUpdateFromServer()));
                    assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAuthAccess.getLastTokenRequest()));
                    assertEquals("fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.", groupAuthAccess.getToken());
                    break;
                case 2:
                    assertEquals(2, groupAuthAccess.getGroupId());
                    assertEquals(true, groupAuthAccess.wasInterrupted());
                    assertEquals(true, groupAuthAccess.isSynchronize());
                    assertEquals(ISO8601Utils.format(otherDate), ISO8601Utils.format(groupAuthAccess.getLastUpdateFromServer()));
                    assertEquals(ISO8601Utils.format(otherDate), ISO8601Utils.format(groupAuthAccess.getLastTokenRequest()));
                    assertEquals("fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.", groupAuthAccess.getToken());
                    break;
                default:
                    assertTrue(false);
            }
        }

    }

    public void testGetGroupAuthAccessesBySynchronize() throws Exception {

        Date currentDate = new Date();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        Date otherDate = new Date();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 2);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, true);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, false);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(otherDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(otherDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        List<GroupAuthAccess> groupAuthAccessList = mGroupAuthAccessDbController.getGroupAuthAccesses(true);
        assertNotNull(groupAuthAccessList);
        assertEquals(1, groupAuthAccessList.size());

        for (GroupAuthAccess groupAuthAccess : groupAuthAccessList) {
            assertNotNull(groupAuthAccess);
            switch (groupAuthAccess.getGroupId()) {
                case 1:
                    assertEquals(1, groupAuthAccess.getGroupId());
                    assertEquals(false, groupAuthAccess.wasInterrupted());
                    assertEquals(true, groupAuthAccess.isSynchronize());
                    assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAuthAccess.getLastUpdateFromServer()));
                    assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAuthAccess.getLastTokenRequest()));
                    assertEquals("fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.", groupAuthAccess.getToken());
                    break;
                default:
                    assertTrue(false);
            }
        }

    }

    public void testGetGroupAuthAccessesSinceDate() throws Exception {

        Date currentDate = new Date(1230);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        Date otherDate = new Date();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 2);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, true);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, false);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(otherDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(otherDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        List<GroupAuthAccess> groupAuthAccessList = mGroupAuthAccessDbController.getGroupAuthAccesses(ISO8601Utils.format(otherDate));
        assertNotNull(groupAuthAccessList);
        assertEquals(1, groupAuthAccessList.size());

        for (GroupAuthAccess groupAuthAccess : groupAuthAccessList) {
            assertNotNull(groupAuthAccess);
            switch (groupAuthAccess.getGroupId()) {
                case 2:
                    assertEquals(2, groupAuthAccess.getGroupId());
                    assertEquals(true, groupAuthAccess.wasInterrupted());
                    assertEquals(false, groupAuthAccess.isSynchronize());
                    assertEquals(ISO8601Utils.format(otherDate), ISO8601Utils.format(groupAuthAccess.getLastUpdateFromServer()));
                    assertEquals(ISO8601Utils.format(otherDate), ISO8601Utils.format(groupAuthAccess.getLastTokenRequest()));
                    assertEquals("fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.", groupAuthAccess.getToken());
                    break;
                default:
                    assertTrue(false);
            }
        }

    }

    public void testUpdate() throws Exception {
        Date currentDate = new Date();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        GroupAuthAccess groupAuthAccess = new GroupAuthAccess(1, "fdskhbvvkddscddueasdfeSAdnandk3229df-dFSJDKMds.");
        groupAuthAccess.setLastTokenRequest(currentDate);
        groupAuthAccess.setLastUpdateFromServer(currentDate);
        groupAuthAccess.setSynchronize(true);
        groupAuthAccess.setInterrupted(true);

        assertTrue(mGroupAuthAccessDbController.update(groupAuthAccess));

        Cursor cursor = db.query(GroupAuthAccess.TABLE_NAME, GroupAuthAccess.COLUMN.ALL_COLUMNS, GroupAuthAccess.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0)
            cursor.close();

        assertTrue(cursor.moveToFirst());
        int groupId = cursor.getInt(cursor.getColumnIndex(GroupAuthAccess.COLUMN.GROUP_ID));
        boolean synchronize = cursor.getInt(cursor.getColumnIndex(GroupAuthAccess.COLUMN.SYNCHRONIZE)) == 1;
        boolean interrupted = cursor.getInt(cursor.getColumnIndex(GroupAuthAccess.COLUMN.INTERRUPTED)) == 1;
        Date lastTokenRequestDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST)), new ParsePosition(0));
        Date lastUpdateDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER)), new ParsePosition(0));
        String token = cursor.getString(cursor.getColumnIndex(GroupAuthAccess.COLUMN.TOKEN));
        cursor.close();

        assertEquals(1, groupId);
        assertEquals(true, synchronize);
        assertEquals(true, interrupted);
        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(lastTokenRequestDate));
        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(lastUpdateDate));

        assertEquals("fdskhbvvkddscddueasdfeSAdnandk3229df-dFSJDKMds.", token);

    }

    public void testUpdateToken() throws Exception {
        Date currentDate = new Date();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        assertTrue(mGroupAuthAccessDbController.updateToken(1, "fdskhbvvkddscddueasdfeSAdnandk3229df-dFSJDKMds."));

        Cursor cursor = db.query(GroupAuthAccess.TABLE_NAME, GroupAuthAccess.COLUMN.ALL_COLUMNS, GroupAuthAccess.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0)
            cursor.close();

        assertTrue(cursor.moveToFirst());
        int groupId = cursor.getInt(cursor.getColumnIndex(GroupAuthAccess.COLUMN.GROUP_ID));
        boolean synchronize = cursor.getInt(cursor.getColumnIndex(GroupAuthAccess.COLUMN.SYNCHRONIZE)) == 1;
        boolean interrupted = cursor.getInt(cursor.getColumnIndex(GroupAuthAccess.COLUMN.INTERRUPTED)) == 1;
        Date lastTokenRequestDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST)), new ParsePosition(0));
        Date lastUpdateDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER)), new ParsePosition(0));
        String token = cursor.getString(cursor.getColumnIndex(GroupAuthAccess.COLUMN.TOKEN));
        cursor.close();

        assertEquals(1, groupId);
        assertEquals(true, synchronize);
        assertEquals(false, interrupted);

        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(lastTokenRequestDate));
        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(lastUpdateDate));
        assertEquals("fdskhbvvkddscddueasdfeSAdnandk3229df-dFSJDKMds.", token);
    }

    public void testHasIdInDatabase() throws Exception {
        Date currentDate = new Date();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuthAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuthAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAuthAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAuthAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAuthAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAuthAccess.TABLE_NAME, null, cv) >= 0);

        assertTrue(mGroupAuthAccessDbController.hasIdInDatabase(1));
        assertFalse(mGroupAuthAccessDbController.hasIdInDatabase(2));

    }
}