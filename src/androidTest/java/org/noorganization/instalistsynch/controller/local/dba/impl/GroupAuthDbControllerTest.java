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

import org.noorganization.instalistsynch.controller.local.dba.IGroupAuthDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupAuth;

import java.util.List;

/**
 * Test for {@link IGroupAuthDbController}
 * Created by tinos_000 on 09.02.2016.
 */
public class GroupAuthDbControllerTest extends AndroidTestCase {

    private Context mContext;
    private IGroupAuthDbController mGroupAuthDbController;
    private SynchDbHelper mDbHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mGroupAuthDbController = LocalSqliteDbControllerFactory.getGroupAuthDbController(mContext);
        mDbHelper = new SynchDbHelper(mContext);
        mDbHelper.onUpgrade(mDbHelper.getWritableDatabase(), 1, 1);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mContext = null;
    }

    public void testInsertRegisteredGroup() throws Exception {
        GroupAuth groupAuth = new GroupAuth(1, 1, "kdsfjsdifjpjf", "TEST_DEVICE", true);
        assertTrue(mGroupAuthDbController.insertRegisteredGroup(groupAuth));

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.query(GroupAuth.TABLE_NAME, GroupAuth.COLUMN.ALL_COLUMNS, GroupAuth.COLUMN.GROUP_ID + " = ? AND " + GroupAuth.COLUMN.DEVICE_ID + " = ? ", new String[]{String.valueOf(1), String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0)
            cursor.close();
        assertEquals(1, count);

        cursor.moveToFirst();

        int groupId = cursor.getInt(cursor.getColumnIndex(GroupAuth.COLUMN.GROUP_ID));
        int deviceId = cursor.getInt(cursor.getColumnIndex(GroupAuth.COLUMN.DEVICE_ID));
        String secret = cursor.getString(cursor.getColumnIndex(GroupAuth.COLUMN.SECRET));
        String deviceName = cursor.getString(cursor.getColumnIndex(GroupAuth.COLUMN.DEVICE_NAME));
        boolean isLocal = cursor.getInt(cursor.getColumnIndex(GroupAuth.COLUMN.IS_LOCAL)) == 1;
        cursor.close();

        assertEquals(1, groupId);
        assertEquals(1, deviceId);
        assertEquals("kdsfjsdifjpjf", secret);
        assertEquals("TEST_DEVICE", deviceName);
        assertEquals(true, isLocal);
    }

    public void testInsertGroupTwice() {
        GroupAuth groupAuth = new GroupAuth(1, 1, "kdsfjsdifjpjf", "TEST_DEVICE", true);
        assertTrue(mGroupAuthDbController.insertRegisteredGroup(groupAuth));
        assertFalse(mGroupAuthDbController.insertRegisteredGroup(groupAuth));

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Cursor cursor = db.query(GroupAuth.TABLE_NAME, GroupAuth.COLUMN.ALL_COLUMNS, GroupAuth.COLUMN.GROUP_ID + " = ? AND " + GroupAuth.COLUMN.DEVICE_ID + " = ? ", new String[]{String.valueOf(1), String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count == 0)
            cursor.close();
        assertEquals(1, count);

        cursor.moveToFirst();

        int groupId = cursor.getInt(cursor.getColumnIndex(GroupAuth.COLUMN.GROUP_ID));
        int deviceId = cursor.getInt(cursor.getColumnIndex(GroupAuth.COLUMN.DEVICE_ID));
        String secret = cursor.getString(cursor.getColumnIndex(GroupAuth.COLUMN.SECRET));
        String deviceName = cursor.getString(cursor.getColumnIndex(GroupAuth.COLUMN.DEVICE_NAME));
        boolean isLocal = cursor.getInt(cursor.getColumnIndex(GroupAuth.COLUMN.IS_LOCAL)) == 1;
        cursor.close();

        assertEquals(1, groupId);
        assertEquals(1, deviceId);
        assertEquals("kdsfjsdifjpjf", secret);
        assertEquals("TEST_DEVICE", deviceName);
        assertEquals(true, isLocal);
    }

    public void testGetRegisteredGroups() throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(GroupAuth.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifjpjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, true);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);

        List<GroupAuth> elems = mGroupAuthDbController.getRegisteredGroups();
        assertEquals(1, elems.size());
        for (GroupAuth elem : elems) {
            assertNotNull(elem);
            switch (elem.getGroupId()) {
                case 1:
                    assertEquals(1, elem.getGroupId());
                    assertEquals(1, elem.getDeviceId());
                    assertEquals("kdsfjsdifjpjf", elem.getSecret());
                    assertEquals("TEST_DEVICE", elem.getDeviceName());
                    assertEquals(true, elem.isLocal());
                    break;
                default:
                    assertTrue(false);
            }
        }
    }

    public void testRemoveRegisteredGroup() throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuth.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifddfgffjpjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, true);

        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);

        cv.put(GroupAuth.COLUMN.GROUP_ID, 3);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 2);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE2");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifjpgdfgdsgfjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, false);

        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);

        cv.put(GroupAuth.COLUMN.GROUP_ID, 2);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE3");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifjaasdasdpjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, false);

        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);

        assertEquals(1, mGroupAuthDbController.removeRegisteredGroup(1));

        // check if whole entry deleted
        Cursor cursor = db.query(GroupAuth.TABLE_NAME, GroupAuth.COLUMN.ALL_COLUMNS, GroupAuth.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        assertEquals(0, count);

        // check if not any side effect happened
        Cursor cursor2 = db.query(GroupAuth.TABLE_NAME, GroupAuth.COLUMN.ALL_COLUMNS, GroupAuth.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(2)}, null, null, null);
        int count2 = cursor2.getCount();
        cursor2.close();
        assertEquals(1, count2);
    }

    public void testFindById() throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuth.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifddfgffjpjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, true);
        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);
        cv.put(GroupAuth.COLUMN.GROUP_ID, 3);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 2);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE2");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifjpgdfgdsgfjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, false);

        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);

        GroupAuth groupAuth = mGroupAuthDbController.findById(1);

        assertEquals(1, groupAuth.getGroupId());
        assertEquals(1, groupAuth.getDeviceId());
        assertEquals("TEST_DEVICE", groupAuth.getDeviceName());
        assertEquals("kdsfjsdifddfgffjpjf", groupAuth.getSecret());
        assertEquals(true, groupAuth.isLocal());
    }

    public void testFindByIdNonExistent() throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuth.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifddfgffjpjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, true);

        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);
        cv.put(GroupAuth.COLUMN.GROUP_ID, 3);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 2);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE2");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifjpgdfgdsgfjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, false);

        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);

        assertNull(mGroupAuthDbController.findById(4));
    }

    public void testHasUniqueId() throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(GroupAuth.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifddfgffjpjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, true);
        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);

        assertFalse(mGroupAuthDbController.hasUniqueId(1));
        assertTrue(mGroupAuthDbController.hasUniqueId(2));

    }

    public void testHasOwnLocalGroup() throws Exception {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assertFalse(mGroupAuthDbController.hasOwnLocalGroup());

        ContentValues cv = new ContentValues();
        cv.put(GroupAuth.COLUMN.GROUP_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, 1);
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, "TEST_DEVICE");
        cv.put(GroupAuth.COLUMN.SECRET, "kdsfjsdifddfgffjpjf");
        cv.put(GroupAuth.COLUMN.IS_LOCAL, true);
        assertTrue(db.insert(GroupAuth.TABLE_NAME, null, cv) >= 0);

        assertTrue(mGroupAuthDbController.hasOwnLocalGroup());

    }
}