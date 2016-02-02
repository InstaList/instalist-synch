package org.noorganization.instalistsynch.controller.local.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.noorganization.instalistsynch.controller.local.IGroupAuthDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.Group;
import org.noorganization.instalistsynch.model.GroupAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of a controller for {@link org.noorganization.instalistsynch.model.GroupAuth}
 * Created by tinos_000 on 29.01.2016.
 */
public class GroupAuthDbController implements IGroupAuthDbController {

    private static GroupAuthDbController sInstance;
    private SynchDbHelper mDbHelper;

    /**
     * Get an instance of {@link GroupAuthDbController}.
     *
     * @param _context the context of the application.
     * @return the singleton object of this class.
     */
    public static GroupAuthDbController getInstance(Context _context) {
        if (sInstance == null) {
            sInstance = new GroupAuthDbController(_context);
        }
        return sInstance;
    }

    private GroupAuthDbController(Context _context) {
        mDbHelper = new SynchDbHelper(_context);
    }

    @Override
    public List<GroupAuth> getRegisteredGroups() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authCursor = db.query(GroupAuth.TABLE_NAME, GroupAuth.COLUMN.ALL_COLUMNS, null, null, null, null, null);
        List<GroupAuth> groupAuths = new ArrayList<>(authCursor.getCount());
        if (!authCursor.moveToFirst()) {
            return groupAuths;
        }

        do {
            String deviceId = authCursor.getString(authCursor.getColumnIndex(GroupAuth.COLUMN.DEVICE_ID));
            String secret = authCursor.getString(authCursor.getColumnIndex(GroupAuth.COLUMN.SECRET));
            String deviceName = authCursor.getString(authCursor.getColumnIndex(GroupAuth.COLUMN.DEVICE_NAME));
            boolean isLocal = authCursor.getInt(authCursor.getColumnIndex(GroupAuth.COLUMN.IS_LOCAL)) == 1;
            // TODO some decryption
            groupAuths.add(new GroupAuth(deviceId, secret, deviceName, isLocal));
        } while (authCursor.moveToNext());
        authCursor.close();
        return groupAuths;
    }

    @Override
    public boolean insertRegisteredGroup(GroupAuth _groupAuth) {
        if (!hasUniqueId(_groupAuth))
            return false;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues(2);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, _groupAuth.getDeviceId());
        // TODO add some encryption
        cv.put(GroupAuth.COLUMN.SECRET, _groupAuth.getSecret());
        cv.put(GroupAuth.COLUMN.DEVICE_NAME, _groupAuth.getDeviceName());
        cv.put(GroupAuth.COLUMN.IS_LOCAL, _groupAuth.isLocal());
        return db.insert(GroupAuth.TABLE_NAME, null, cv) != -1;
    }

    @Override
    public int removeRegisteredGroup(GroupAuth _groupAuth) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        return db.delete(GroupAuth.TABLE_NAME, GroupAuth.COLUMN.DEVICE_ID + " LIKE ?", new String[]{_groupAuth.getDeviceId()});
    }

    @Override
    public boolean hasUniqueId(GroupAuth _groupAuth) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authCursor = db.query(GroupAuth.TABLE_NAME, GroupAuth.COLUMN.ALL_COLUMNS, GroupAuth.COLUMN.DEVICE_ID + " LIKE ?", new String[]{_groupAuth.getDeviceId()}, null, null, null);
        boolean ret = authCursor.getCount() == 0;
        authCursor.close();
        return ret;
    }

    @Override
    public boolean hasOwnLocalGroup() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor authCursor = db.query(GroupAuth.TABLE_NAME, GroupAuth.COLUMN.ALL_COLUMNS, GroupAuth.COLUMN.IS_LOCAL + " = ?", new String[]{"1"}, null, null, null);
        boolean ret = authCursor.getCount() >= 1;
        authCursor.close();
        return ret;
    }
}
