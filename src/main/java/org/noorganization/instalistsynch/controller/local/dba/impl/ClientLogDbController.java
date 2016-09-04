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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.types.ActionType;
import org.noorganization.instalist.types.ModelType;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalistsynch.controller.local.dba.IClientLogDbController;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Controller to access the client log.
 * Created by Desnoo on 16.02.2016.
 */
public class ClientLogDbController implements IClientLogDbController {

    private static ClientLogDbController sInstance;
    private ContentResolver mContentResolver;

    private ClientLogDbController(Context _context) {
        mContentResolver = _context.getContentResolver();
    }

    public static ClientLogDbController getInstance(Context _context) {
        if (sInstance == null) {
            sInstance = new ClientLogDbController(_context);
        }
        return sInstance;
    }

    @Override
    public Cursor getLogs() {
        return mContentResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "log"),
                LogInfo.COLUMN.ALL_COLUMNS, null, null,
                LogInfo.COLUMN.ACTION_DATE + " DESC ");
    }

    @Override
    public Cursor getLogsSince(String _date, @ModelType.Model int _modelType) {
        return mContentResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "log"),
                LogInfo.COLUMN.ALL_COLUMNS,
                LogInfo.COLUMN.MODEL + " = ? AND " + LogInfo.COLUMN.ACTION_DATE + " >=  ? ",
                new String[]{String.valueOf(_modelType), _date},
                LogInfo.COLUMN.ACTION_DATE + " ASC ");
    }

    @Override
    public List<LogInfo> getElementByUuid(
            String _uuid, @ActionType.Action int _actionType, @ModelType.Model int _modelType, String _time) {
        Cursor cursor = mContentResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "log"),
                LogInfo.COLUMN.ALL_COLUMNS,
                LogInfo.COLUMN.ITEM_UUID + " LIKE ? AND "
                        + LogInfo.COLUMN.ACTION + " = ? AND "
                        + LogInfo.COLUMN.MODEL + " = ? AND "
                        + LogInfo.COLUMN.ACTION_DATE + " >= ? ",
                new String[]{_uuid, String.valueOf(_actionType),
                        String.valueOf(_modelType), _time},
                null);
        if (cursor == null) {
            return new ArrayList<>();
        }

        if (cursor.getCount() == 0) {
            cursor.close();
            return new ArrayList<>();
        }

        List<LogInfo> list = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        try {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(LogInfo.COLUMN.ID));
                String uuid = cursor.getString(cursor.getColumnIndex(LogInfo.COLUMN.ITEM_UUID));
                @ActionType.Action int action = cursor.getInt(cursor.getColumnIndex(LogInfo.COLUMN.ACTION));
                @ModelType.Model int model = cursor.getInt(cursor.getColumnIndex(LogInfo.COLUMN.MODEL));
                String date = cursor.getString(cursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));

                list.add(new LogInfo(id, uuid, action, model,
                        ISO8601Utils.parse(date, new ParsePosition(0))));
            }
            while (cursor.moveToNext());
        } catch (ParseException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            cursor.close();
        }
        return list;
    }

    @Override
    public Date getLeastRecentUpdateTimeForUuid(String _clientUuid) {
        Cursor cursor = mContentResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "log"),
                LogInfo.COLUMN.ALL_COLUMNS,
                LogInfo.COLUMN.ITEM_UUID + " LIKE ? ", new String[]{_clientUuid},
                LogInfo.COLUMN.ACTION_DATE + " DESC ");
        if (cursor == null) {
            return null;
        }
        if (cursor.getCount() == 0) {
            cursor.close();
            return null;
        }
        cursor.moveToFirst();

        String date = cursor.getString(cursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE));
        try {
            return ISO8601Utils.parse(date, new ParsePosition(0));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        } finally {
            cursor.close();
        }
    }
}
