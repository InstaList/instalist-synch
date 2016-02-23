package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.LogInfo;
import org.noorganization.instalist.provider.InstalistProvider;
import org.noorganization.instalistsynch.controller.local.dba.IClientLogDbController;

import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller to access the client log.
 * Created by Desnoo on 16.02.2016.
 */
public class ClientLogDbController implements IClientLogDbController {

    private static ClientLogDbController sInstance;
    private        ContentResolver       mContentResolver;

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
    public Cursor getLogsSince(String _date, eModelType _modelType) {
        return mContentResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "log"),
                LogInfo.COLUMN.ALL_COLUMNS,
                LogInfo.COLUMN.MODEL + " = ? AND " + LogInfo.COLUMN.ACTION_DATE + " >=  ? ",
                new String[]{String.valueOf(_modelType.ordinal()), _date},
                LogInfo.COLUMN.ACTION_DATE + " DESC ");
    }

    @Override
    public List<LogInfo> getElementByUuid(
            String _uuid, eActionType _actionType, eModelType _modelType, String _time) {
        Cursor cursor = mContentResolver.query(
                Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "log"),
                LogInfo.COLUMN.ALL_COLUMNS,
                LogInfo.COLUMN.ITEM_UUID + " LIKE ? AND "
                        + LogInfo.COLUMN.ACTION + " = ? AND "
                        + LogInfo.COLUMN.MODEL + " = ? AND "
                        + LogInfo.COLUMN.ACTION_DATE + " >= ? ",
                new String[]{_uuid, String.valueOf(_actionType.ordinal()),
                        String.valueOf(_modelType.ordinal()), _time},
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
                int action = cursor.getInt(cursor.getColumnIndex(LogInfo.COLUMN.ACTION));
                eActionType actionType = eActionType.getTypeById(action);
                int model = cursor.getInt(cursor.getColumnIndex(LogInfo.COLUMN.MODEL));
                eModelType modelType = eModelType.getTypeId(model);
                String date =
                        cursor.getString(cursor.getColumnIndex(LogInfo.COLUMN.ACTION_DATE)).concat(
                                "+00:00");

                list.add(new LogInfo(id, uuid, actionType, modelType,
                        ISO8601Utils.parse(date, new ParsePosition(0))));
            } while (cursor.moveToNext());
        } catch (ParseException e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            cursor.close();
        }
        return list;
    }
}
