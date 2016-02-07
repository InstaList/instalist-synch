package org.noorganization.instalistsynch.controller.local.impl;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import org.noorganization.instalistsynch.controller.local.IGroupMemberDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupMember;

/**
 * Tests of the GroupMemberDbController.
 * Created by Desnoo on 05.02.2016.
 */
public class GroupMemberDbControllerTest extends AndroidTestCase {

    private Context mContext;
    private IGroupMemberDbController mIGroupMemberDbController;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = getContext();
        mIGroupMemberDbController = LocalSqliteDbControllerFactory.getGroupMemberDbController(mContext);
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
        GroupMember groupMember = new GroupMember(null, "123532", "123452", "TEST_DEVICE", true);
        GroupMember insertedGroupMember = mIGroupMemberDbController.insert(groupMember);

        assertNotNull(insertedGroupMember);
        assertNotNull(insertedGroupMember.getUUID());
        SynchDbHelper dbHelper = new SynchDbHelper(mContext);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] selectionArgs = {insertedGroupMember.getUUID()};
        Cursor cursor = db.query(false, GroupMember.TABLE_NAME, GroupMember.COLUMN.ALL_COLUMNS, GroupMember.COLUMN.ID + " LIKE ?", selectionArgs, null, null, null, null);

        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(insertedGroupMember.getUUID(), cursor.getString(cursor.getColumnIndex(GroupMember.COLUMN.ID)));
        assertEquals("TEST_DEVICE", cursor.getString(cursor.getColumnIndex(GroupMember.COLUMN.NAME)));
        assertEquals("123532", cursor.getString(cursor.getColumnIndex(GroupMember.COLUMN.DEVICE_ID)));
        assertEquals("123452", cursor.getString(cursor.getColumnIndex(GroupMember.COLUMN.OWN_DEVICE_ID)));
        assertEquals(true, cursor.getInt(cursor.getColumnIndex(GroupMember.COLUMN.AUTHORIZED)) == 1);

        cursor.close();
    }

    public void testUpdate() throws Exception {

    }

    public void testDelete() throws Exception {

    }

    public void testGetById() throws Exception {

    }

    public void testGetByOwnerId() throws Exception {

    }
}