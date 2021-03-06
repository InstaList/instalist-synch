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

import org.noorganization.instalistsynch.controller.local.dba.ITaskErrorLogDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.TaskErrorLog;

import java.util.ArrayList;
import java.util.List;

/**
 * The controller for TaskErrorLogs.
 * Created by Desnoo on 15.02.2016.
 */
public class TaskErrorLogDbController implements ITaskErrorLogDbController {

    private static TaskErrorLogDbController sInstance;

    private SynchDbHelper mSynchDbHelper;

    public static TaskErrorLogDbController getInstance(Context _context) {
        if (sInstance == null) {
            sInstance = new TaskErrorLogDbController(_context);
        }
        return sInstance;
    }

    private TaskErrorLogDbController(Context _context) {
        mSynchDbHelper = new SynchDbHelper(_context);
    }

    @Override
    public TaskErrorLog insert(String _uuid, int _type, int _errorType, int _groupId) {
        SQLiteDatabase db = mSynchDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(TaskErrorLog.COLUMN.GROUP_ID, _groupId);
        cv.put(TaskErrorLog.COLUMN.ERROR_TYPE, _errorType);
        cv.put(TaskErrorLog.COLUMN.TYPE, _type);
        cv.put(TaskErrorLog.COLUMN.SERVER_UUID, _uuid);

        long row = db.insert(TaskErrorLog.TABLE_NAME, null, cv);
        if (row == -1)
            return null;

        // TODO get by id
        return null;
    }

    @Override
    public List<TaskErrorLog> get(int _groupId, int _type, int _errorType) {
        SQLiteDatabase db = mSynchDbHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskErrorLog.TABLE_NAME, TaskErrorLog.COLUMN.ALL_COLUMNS,
                TaskErrorLog.COLUMN.GROUP_ID + " = ? AND " + TaskErrorLog.COLUMN.TYPE + " = ? AND " + TaskErrorLog.COLUMN.ERROR_TYPE + " = ?",
                new String[]{String.valueOf(_groupId), String.valueOf(_type), String.valueOf(_errorType)}, null, null, null, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            return new ArrayList<>(0);
        }

        cursor.moveToFirst();
        List<TaskErrorLog> ret = new ArrayList<>(cursor.getCount());
        do {
            String uuid = cursor.getString(cursor.getColumnIndex(TaskErrorLog.COLUMN.SERVER_UUID));
            int id = cursor.getInt(cursor.getColumnIndex(TaskErrorLog.COLUMN.ID));
            int errorType = cursor.getInt(cursor.getColumnIndex(TaskErrorLog.COLUMN.ERROR_TYPE));
            int type = cursor.getInt(cursor.getColumnIndex(TaskErrorLog.COLUMN.TYPE));
            int groupId = cursor.getInt(cursor.getColumnIndex(TaskErrorLog.COLUMN.GROUP_ID));
            ret.add(new TaskErrorLog(id, uuid, groupId, type, errorType));
        } while (cursor.moveToNext());
        cursor.close();

        return ret;
    }

    @Override
    public TaskErrorLog findById(int _id) {
        SQLiteDatabase db = mSynchDbHelper.getReadableDatabase();
        Cursor cursor = db.query(TaskErrorLog.TABLE_NAME, TaskErrorLog.COLUMN.ALL_COLUMNS,
                TaskErrorLog.COLUMN.ID + " = ? ",
                new String[]{String.valueOf(_id)}, null, null, null, null);

        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }

        cursor.moveToFirst();
        String uuid = cursor.getString(cursor.getColumnIndex(TaskErrorLog.COLUMN.SERVER_UUID));
        int id = cursor.getInt(cursor.getColumnIndex(TaskErrorLog.COLUMN.ID));
        int errorType = cursor.getInt(cursor.getColumnIndex(TaskErrorLog.COLUMN.ERROR_TYPE));
        int type = cursor.getInt(cursor.getColumnIndex(TaskErrorLog.COLUMN.TYPE));
        int groupId = cursor.getInt(cursor.getColumnIndex(TaskErrorLog.COLUMN.GROUP_ID));

        TaskErrorLog errorLog = new TaskErrorLog(id, uuid, groupId, type, errorType);
        cursor.close();

        return errorLog;
    }

    @Override
    public void remove(int _id) {
        SQLiteDatabase db = mSynchDbHelper.getWritableDatabase();
        db.delete(TaskErrorLog.TABLE_NAME, TaskErrorLog.COLUMN.ID + " = ?", new String[]{String.valueOf(_id)});
    }
}
