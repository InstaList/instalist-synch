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

import org.noorganization.instalistsynch.controller.local.dba.IGroupMemberDbController;
import org.noorganization.instalistsynch.controller.local.dba.LocalSqliteDbControllerFactory;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.AccessRight;
import org.noorganization.instalistsynch.model.GroupMember;

import java.util.List;

/**
 * Tests of the GroupMemberDbController.
 * Created by Desnoo on 05.02.2016.
 */
public class GroupMemberDbControllerTest extends AndroidTestCase {

    private Context mContext;
    private IGroupMemberDbController mIGroupMemberDbController;
    private SynchDbHelper mDbHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mIGroupMemberDbController = LocalSqliteDbControllerFactory.getGroupMemberDbController(mContext);
        mDbHelper = new SynchDbHelper(mContext);
        mDbHelper.onUpgrade(mDbHelper.getWritableDatabase(), 1, 1);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mContext = null;
    }

    public void testGetInstance() throws Exception {
        GroupMemberDbController dbController = GroupMemberDbController.getInstance(mContext);
        assertNotNull(dbController);
        assertTrue(dbController instanceof GroupMemberDbController);
    }

    public void testInsertSingleGroupMember() throws Exception {
        GroupMember groupMember = new GroupMember(123, 123532, "TEST_DEVICE", new AccessRight(true, true));
        GroupMember insertedGroupMember = mIGroupMemberDbController.insert(groupMember);

        assertNotNull(insertedGroupMember);
        assertEquals(123, insertedGroupMember.getGroupId());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] selectionArgs = {String.valueOf(insertedGroupMember.getGroupId())};
        Cursor cursor = db.query(false, GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ?", selectionArgs, null, null, null, null);
        int count = cursor.getCount();
        if (count != 1)
            cursor.close();
        assertEquals(1, count);
        cursor.moveToFirst();

        int groupId = cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.GROUP_ID));
        int deviceId = cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.DEVICE_ID));
        String name = cursor.getString(cursor.getColumnIndex(GroupMember.COLUMN.NAME));
        boolean access = cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.AUTHORIZED)) == 1;

        cursor.close();

        cursor.moveToFirst();
        assertEquals(123, groupId);
        assertEquals(123532, deviceId);
        assertEquals("TEST_DEVICE", name);
        assertEquals(true, access);
    }

    public void testInsertSingleEmptyGroupMember() throws Exception {
        GroupMember groupMember = new GroupMember(123, 123532, null, new AccessRight(true, true));
        GroupMember insertedGroupMember = mIGroupMemberDbController.insert(groupMember);


        assertNull(insertedGroupMember);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] selectionArgs = {String.valueOf(123)};
        Cursor cursor = db.query(false, GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ?", selectionArgs, null, null, null, null);

        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    public void testInsertSameGroupId() {
        GroupMember groupMember = new GroupMember(123, 123532, "TEST_DEVICE", new AccessRight(true, true));
        GroupMember insertedGroupMember = mIGroupMemberDbController.insert(groupMember);
        assertNotNull(insertedGroupMember);
        assertEquals(123, insertedGroupMember.getGroupId());
        assertEquals(123532, insertedGroupMember.getDeviceId());

        GroupMember groupMember2 = new GroupMember(123, 123534, "TEST_DEVICE2", new AccessRight(true, true));
        GroupMember insertedgroupMember2 = mIGroupMemberDbController.insert(groupMember2);

        assertNotNull(insertedgroupMember2);
        assertEquals(123, insertedgroupMember2.getGroupId());
        assertEquals(123534, insertedgroupMember2.getDeviceId());

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] selectionArgs = {String.valueOf(123)};
        Cursor cursor = db.query(false, GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ?", selectionArgs, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        assertEquals(2, count);

    }

    public void testInsertSameGroupAndDeviceId() {
        GroupMember groupMember = new GroupMember(123, 123532, "TEST_DEVICE", new AccessRight(true, true));
        GroupMember insertedGroupMember = mIGroupMemberDbController.insert(groupMember);
        assertNotNull(insertedGroupMember);
        assertEquals(123, insertedGroupMember.getGroupId());
        assertEquals(123532, insertedGroupMember.getDeviceId());

        GroupMember groupMember2 = new GroupMember(123, 123532, "TEST_DEVICE2", new AccessRight(true, true));
        GroupMember insertedgroupMember2 = mIGroupMemberDbController.insert(groupMember2);

        assertNull(insertedgroupMember2);

        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        String[] selectionArgs = {String.valueOf(insertedGroupMember.getGroupId())};
        Cursor cursor = db.query(false, GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ?", selectionArgs, null, null, null, null);
        int count = cursor.getCount();
        cursor.close();

        assertEquals(1, count);
    }

    public void testUpdate() throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.GROUP_ID, 1);
        cv.put(GroupMember.COLUMN.DEVICE_ID, 1);
        cv.put(GroupMember.COLUMN.AUTHORIZED, true);
        cv.put(GroupMember.COLUMN.NAME, "TEST_DEVICE");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assertTrue(db.insert(GroupMember.TABLE_NAME, null, cv) >= 0);

        assertTrue(mIGroupMemberDbController.update(new GroupMember(1, 1, "TEST_DEVICE_NEW", new AccessRight(true, true))));

        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count != 1)
            cursor.close();
        assertEquals(1, count);
        assertTrue(cursor.moveToFirst());

        int groupId = cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.GROUP_ID));
        int deviceId = cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.DEVICE_ID));
        String name = cursor.getString(cursor.getColumnIndex(GroupMember.COLUMN.NAME));
        boolean access = cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.AUTHORIZED)) == 1;

        cursor.close();

        assertEquals(1, groupId);
        assertEquals(1, deviceId);
        assertEquals("TEST_DEVICE_NEW", name);
        assertEquals(true, access);

    }

    public void testUpdateNonExisting() throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.GROUP_ID, 1);
        cv.put(GroupMember.COLUMN.DEVICE_ID, 1);
        cv.put(GroupMember.COLUMN.AUTHORIZED, true);
        cv.put(GroupMember.COLUMN.NAME, "TEST_DEVICE");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assertTrue(db.insert(GroupMember.TABLE_NAME, null, cv) >= 0);

        assertFalse(mIGroupMemberDbController.update(new GroupMember(1, 2, "TEST_DEVICE_NEW", new AccessRight(true, true))));

        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ? ", new String[]{String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        if (count != 1)
            cursor.close();
        assertEquals(1, count);
        assertTrue(cursor.moveToFirst());

        int groupId = cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.GROUP_ID));
        int deviceId = cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.DEVICE_ID));
        String name = cursor.getString(cursor.getColumnIndex(GroupMember.COLUMN.NAME));
        boolean access = cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.AUTHORIZED)) == 1;

        cursor.close();

        assertEquals(1, groupId);
        assertEquals(1, deviceId);
        assertEquals("TEST_DEVICE", name);
        assertEquals(true, access);

    }

    public void testDelete() throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.GROUP_ID, 1);
        cv.put(GroupMember.COLUMN.DEVICE_ID, 1);
        cv.put(GroupMember.COLUMN.AUTHORIZED, true);
        cv.put(GroupMember.COLUMN.NAME, "TEST_DEVICE");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assertTrue(db.insert(GroupMember.TABLE_NAME, null, cv) >= 0);

        assertTrue(mIGroupMemberDbController.delete(1, 1));

        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ? AND " + GroupMember.COLUMN.DEVICE_ID + " = ? ", new String[]{String.valueOf(1), String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        assertEquals(0, count);
    }

    public void testDeleteNonExisting() {
        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.GROUP_ID, 1);
        cv.put(GroupMember.COLUMN.DEVICE_ID, 1);
        cv.put(GroupMember.COLUMN.AUTHORIZED, true);
        cv.put(GroupMember.COLUMN.NAME, "TEST_DEVICE");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assertTrue(db.insert(GroupMember.TABLE_NAME, null, cv) >= 0);

        assertFalse(mIGroupMemberDbController.delete(1, 2));

        Cursor cursor = db.query(GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.GROUP_ID + " = ? AND " + GroupMember.COLUMN.DEVICE_ID + " = ? ", new String[]{String.valueOf(1), String.valueOf(1)}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        assertEquals(1, count);
    }

    public void testGetById() throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.GROUP_ID, 1);
        cv.put(GroupMember.COLUMN.DEVICE_ID, 1);
        cv.put(GroupMember.COLUMN.AUTHORIZED, true);
        cv.put(GroupMember.COLUMN.NAME, "TEST_DEVICE");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assertTrue(db.insert(GroupMember.TABLE_NAME, null, cv) >= 0);

        GroupMember elem = mIGroupMemberDbController.getById(1, 1);
        assertNotNull(elem);

        assertEquals(1, elem.getGroupId());
        assertEquals(1, elem.getDeviceId());
        assertEquals(true, elem.getAccessRights().hasReadRight() && elem.getAccessRights().hasWriteRight());
        assertEquals("TEST_DEVICE", elem.getName());
    }

    public void testGetByGroupId() throws Exception {
        ContentValues cv = new ContentValues();
        cv.put(GroupMember.COLUMN.GROUP_ID, 1);
        cv.put(GroupMember.COLUMN.DEVICE_ID, 1);
        cv.put(GroupMember.COLUMN.AUTHORIZED, true);
        cv.put(GroupMember.COLUMN.NAME, "TEST_DEVICE");

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        assertTrue(db.insert(GroupMember.TABLE_NAME, null, cv) >= 0);
        cv.put(GroupMember.COLUMN.GROUP_ID, 2);
        cv.put(GroupMember.COLUMN.DEVICE_ID, 1);
        cv.put(GroupMember.COLUMN.AUTHORIZED, true);
        cv.put(GroupMember.COLUMN.NAME, "TEST_DEVICE2");
        assertTrue(db.insert(GroupMember.TABLE_NAME, null, cv) >= 0);


        List<GroupMember> elems = mIGroupMemberDbController.getByGroup(1);
        assertEquals(1, elems.size());

        GroupMember member = elems.get(0);
        assertEquals(1, member.getGroupId());
        assertEquals(1, member.getDeviceId());
        assertEquals(true, member.getAccessRights().hasReadRight() && member.getAccessRights().hasWriteRight());
        assertEquals("TEST_DEVICE", member.getName());
    }
}