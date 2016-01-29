package org.noorganization.instalistsynch.controller.local.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.noorganization.instalistsynch.controller.local.IGroupAuthDbController;
import org.noorganization.instalistsynch.db.sqlite.GroupAuthDbHelper;
import org.noorganization.instalistsynch.model.GroupAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of a controller for {@link org.noorganization.instalistsynch.model.GroupAuth}
 * Created by tinos_000 on 29.01.2016.
 */
public class GroupAuthDbController implements IGroupAuthDbController {

    private static GroupAuthDbController sInstance;
    private GroupAuthDbHelper mDbHelper;

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
        mDbHelper = new GroupAuthDbHelper(_context);
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
            // TODO some decryption
            groupAuths.add(new GroupAuth(deviceId, secret));
        } while (authCursor.moveToNext());
        authCursor.close();
        return groupAuths;
    }

    @Override
    public boolean insertRegisteredGroup(GroupAuth _groupAuth) {
        if(!hasUniqueId(_groupAuth))
            return false;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues(2);
        cv.put(GroupAuth.COLUMN.DEVICE_ID, _groupAuth.getDeviceId());
        // TODO add some encryption
        cv.put(GroupAuth.COLUMN.SECRET, _groupAuth.getSecret());
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
}
