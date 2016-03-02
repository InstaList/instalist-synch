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
import android.test.AndroidTestCase;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthAccessDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupAccess;

import java.text.ParsePosition;
import java.util.Date;
import java.util.List;

/**
 * Tests for the sqliteGroupAuthAccessDB Controller
 * Created by tinos_000 on 09.02.2016.
 */
public class SqliteGroupAccessDbControllerTest extends AndroidTestCase {

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
        GroupAccess
                groupAccess = new GroupAccess(1, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");
        groupAccess.setLastTokenRequest(currentDate);
        groupAccess.setLastUpdateFromServer(currentDate);
        groupAccess.setSynchronize(true);
        groupAccess.setInterrupted(false);

        assertEquals(IGroupAuthAccessDbController.INSERTION_CODE.CORRECT, mGroupAuthAccessDbController.insert(
                groupAccess));

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor = db.query(GroupAccess.TABLE_NAME, GroupAccess.COLUMN.ALL_COLUMNS, GroupAccess.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0)
            cursor.close();

        assertTrue(cursor.moveToFirst());

        int groupId = cursor.getInt(cursor.getColumnIndex(GroupAccess.COLUMN.GROUP_ID));
        boolean synchronize = cursor.getInt(cursor.getColumnIndex(GroupAccess.COLUMN.SYNCHRONIZE)) == 1;
        boolean interrupted = cursor.getInt(cursor.getColumnIndex(GroupAccess.COLUMN.INTERRUPTED)) == 1;
        Date lastTokenRequestDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(
                GroupAccess.COLUMN.LAST_TOKEN_REQUEST)), new ParsePosition(0));
        Date lastUpdateDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER)), new ParsePosition(0));
        String token = cursor.getString(cursor.getColumnIndex(GroupAccess.COLUMN.TOKEN));

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
        cv.put(GroupAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        GroupAccess groupAccess = mGroupAuthAccessDbController.getGroupAuthAccess(1);
        assertNotNull(groupAccess);
        assertEquals(1, groupAccess.getGroupId());
        assertEquals(false, groupAccess.wasInterrupted());
        assertEquals(true, groupAccess.isSynchronize());
        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAccess.getLastUpdateFromServer()));
        assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAccess.getLastTokenRequest()));
        assertEquals("fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.", groupAccess.getToken());
    }

    public void testInsertSameId() throws Exception {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        Date currentDate = new Date();
        GroupAccess
                groupAccess = new GroupAccess(1, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");
        groupAccess.setLastTokenRequest(currentDate);
        groupAccess.setLastUpdateFromServer(currentDate);
        groupAccess.setSynchronize(true);
        groupAccess.setInterrupted(false);

        assertEquals(IGroupAuthAccessDbController.INSERTION_CODE.CORRECT, mGroupAuthAccessDbController.insert(
                groupAccess));

        groupAccess = new GroupAccess(1, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");
        groupAccess.setLastTokenRequest(currentDate);
        groupAccess.setLastUpdateFromServer(currentDate);
        groupAccess.setSynchronize(true);
        groupAccess.setInterrupted(false);

        assertEquals(IGroupAuthAccessDbController.INSERTION_CODE.ELEMENT_EXISTS, mGroupAuthAccessDbController.insert(
                groupAccess));
    }

    public void testGetGroupAuthAccesses() throws Exception {

        Date currentDate = new Date();
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        Date otherDate = new Date();
        cv.put(GroupAccess.COLUMN.GROUP_ID, 2);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, true);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(otherDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(otherDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        List<GroupAccess> groupAccessList = mGroupAuthAccessDbController.getGroupAuthAccesses();
        assertNotNull(groupAccessList);
        assertEquals(2, groupAccessList.size());

        for (GroupAccess groupAccess : groupAccessList) {
            assertNotNull(groupAccess);
            switch (groupAccess.getGroupId()) {
                case 1:
                    assertEquals(1, groupAccess.getGroupId());
                    assertEquals(false, groupAccess.wasInterrupted());
                    assertEquals(true, groupAccess.isSynchronize());
                    assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAccess.getLastUpdateFromServer()));
                    assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAccess.getLastTokenRequest()));
                    assertEquals("fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.", groupAccess.getToken());
                    break;
                case 2:
                    assertEquals(2, groupAccess.getGroupId());
                    assertEquals(true, groupAccess.wasInterrupted());
                    assertEquals(true, groupAccess.isSynchronize());
                    assertEquals(ISO8601Utils.format(otherDate), ISO8601Utils.format(groupAccess.getLastUpdateFromServer()));
                    assertEquals(ISO8601Utils.format(otherDate), ISO8601Utils.format(groupAccess.getLastTokenRequest()));
                    assertEquals("fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.", groupAccess.getToken());
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
        cv.put(GroupAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        Date otherDate = new Date();
        cv.put(GroupAccess.COLUMN.GROUP_ID, 2);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, true);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, false);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(otherDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(otherDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        List<GroupAccess> groupAccessList = mGroupAuthAccessDbController.getGroupAuthAccesses(true);
        assertNotNull(groupAccessList);
        assertEquals(1, groupAccessList.size());

        for (GroupAccess groupAccess : groupAccessList) {
            assertNotNull(groupAccess);
            switch (groupAccess.getGroupId()) {
                case 1:
                    assertEquals(1, groupAccess.getGroupId());
                    assertEquals(false, groupAccess.wasInterrupted());
                    assertEquals(true, groupAccess.isSynchronize());
                    assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAccess.getLastUpdateFromServer()));
                    assertEquals(ISO8601Utils.format(currentDate), ISO8601Utils.format(groupAccess.getLastTokenRequest()));
                    assertEquals("fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.", groupAccess.getToken());
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
        cv.put(GroupAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        Date otherDate = new Date();
        cv.put(GroupAccess.COLUMN.GROUP_ID, 2);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, true);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, false);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(otherDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(otherDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        List<GroupAccess> groupAccessList = mGroupAuthAccessDbController.getGroupAuthAccesses(ISO8601Utils.format(otherDate));
        assertNotNull(groupAccessList);
        assertEquals(1, groupAccessList.size());

        for (GroupAccess groupAccess : groupAccessList) {
            assertNotNull(groupAccess);
            switch (groupAccess.getGroupId()) {
                case 2:
                    assertEquals(2, groupAccess.getGroupId());
                    assertEquals(true, groupAccess.wasInterrupted());
                    assertEquals(false, groupAccess.isSynchronize());
                    assertEquals(ISO8601Utils.format(otherDate), ISO8601Utils.format(groupAccess.getLastUpdateFromServer()));
                    assertEquals(ISO8601Utils.format(otherDate), ISO8601Utils.format(groupAccess.getLastTokenRequest()));
                    assertEquals("fdskhbvvkddscddu3rssNDFSAdnandk3229df-dFSJDKMds.", groupAccess.getToken());
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
        cv.put(GroupAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        GroupAccess
                groupAccess = new GroupAccess(1, "fdskhbvvkddscddueasdfeSAdnandk3229df-dFSJDKMds.");
        groupAccess.setLastTokenRequest(currentDate);
        groupAccess.setLastUpdateFromServer(currentDate);
        groupAccess.setSynchronize(true);
        groupAccess.setInterrupted(true);

        assertTrue(mGroupAuthAccessDbController.update(groupAccess));

        Cursor cursor = db.query(GroupAccess.TABLE_NAME, GroupAccess.COLUMN.ALL_COLUMNS, GroupAccess.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0)
            cursor.close();

        assertTrue(cursor.moveToFirst());
        int groupId = cursor.getInt(cursor.getColumnIndex(GroupAccess.COLUMN.GROUP_ID));
        boolean synchronize = cursor.getInt(cursor.getColumnIndex(GroupAccess.COLUMN.SYNCHRONIZE)) == 1;
        boolean interrupted = cursor.getInt(cursor.getColumnIndex(GroupAccess.COLUMN.INTERRUPTED)) == 1;
        Date lastTokenRequestDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(
                GroupAccess.COLUMN.LAST_TOKEN_REQUEST)), new ParsePosition(0));
        Date lastUpdateDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER)), new ParsePosition(0));
        String token = cursor.getString(cursor.getColumnIndex(GroupAccess.COLUMN.TOKEN));
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
        cv.put(GroupAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        assertTrue(mGroupAuthAccessDbController.updateToken(1, "fdskhbvvkddscddueasdfeSAdnandk3229df-dFSJDKMds."));

        Cursor cursor = db.query(GroupAccess.TABLE_NAME, GroupAccess.COLUMN.ALL_COLUMNS, GroupAccess.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0)
            cursor.close();

        assertTrue(cursor.moveToFirst());
        int groupId = cursor.getInt(cursor.getColumnIndex(GroupAccess.COLUMN.GROUP_ID));
        boolean synchronize = cursor.getInt(cursor.getColumnIndex(GroupAccess.COLUMN.SYNCHRONIZE)) == 1;
        boolean interrupted = cursor.getInt(cursor.getColumnIndex(GroupAccess.COLUMN.INTERRUPTED)) == 1;
        Date lastTokenRequestDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(
                GroupAccess.COLUMN.LAST_TOKEN_REQUEST)), new ParsePosition(0));
        Date lastUpdateDate = ISO8601Utils.parse(cursor.getString(cursor.getColumnIndex(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER)), new ParsePosition(0));
        String token = cursor.getString(cursor.getColumnIndex(GroupAccess.COLUMN.TOKEN));
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
        cv.put(GroupAccess.COLUMN.GROUP_ID, 1);
        cv.put(GroupAccess.COLUMN.INTERRUPTED, false);
        cv.put(GroupAccess.COLUMN.SYNCHRONIZE, true);
        cv.put(GroupAccess.COLUMN.LAST_UPDATE_FROM_SERVER, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.LAST_TOKEN_REQUEST, ISO8601Utils.format(currentDate));
        cv.put(GroupAccess.COLUMN.TOKEN, "fdskhbvvkddscddueFSNDFSAdnandk3229df-dFSJDKMds.");

        assertTrue(db.insert(GroupAccess.TABLE_NAME, null, cv) >= 0);

        assertTrue(mGroupAuthAccessDbController.hasIdInDatabase(1));
        assertFalse(mGroupAuthAccessDbController.hasIdInDatabase(2));

    }
}