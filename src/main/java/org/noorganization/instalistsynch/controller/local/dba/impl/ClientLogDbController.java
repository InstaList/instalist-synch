package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.fasterxml.jackson.databind.util.ISO8601Utils;

import org.noorganization.instalist.enums.eActionType;
import org.noorganization.instalist.enums.eModelType;
import org.noorganization.instalist.model.Log;
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

    private ContentResolver mContentResolver;

    private static ClientLogDbController sInstance;

    public static ClientLogDbController getInstance(Context _context) {
        if (sInstance == null) {
            sInstance = new ClientLogDbController(_context);
        }
        return sInstance;
    }

    private ClientLogDbController(Context _context) {
        mContentResolver = _context.getContentResolver();
    }

    @Override
    public Cursor getLogs() {
        return mContentResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "log"), Log.COLUMN.ALL_COLUMNS, null, null, "DESC datetime(" + Log.COLUMN.ACTION_DATE + ")");
    }

    @Override
    public List<Log> getElementByUuid(String _uuid, eActionType _actionType, eModelType _modelType, String _time) {
        Cursor cursor = mContentResolver.query(Uri.withAppendedPath(InstalistProvider.BASE_CONTENT_URI, "log"), Log.COLUMN.ALL_COLUMNS,
                Log.COLUMN.ITEM_UUID + " LIKE ? AND "
                        + Log.COLUMN.ACTION + " = ? AND "
                        + Log.COLUMN.MODEL + " = ? "
                        + Log.COLUMN.ACTION_DATE + " >= datetime( ? )"
                        + ")",
                new String[]{_uuid, String.valueOf(_actionType.ordinal()), String.valueOf(_modelType.ordinal()), _time},
                null);
        if (cursor == null)
            return new ArrayList<>();

        if (cursor.getCount() == 0) {
            cursor.close();
            return new ArrayList<>();
        }

        List<Log> list = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        try {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(Log.COLUMN.ID));
                String uuid = cursor.getString(cursor.getColumnIndex(Log.COLUMN.ITEM_UUID));
                int action = cursor.getInt(cursor.getColumnIndex(Log.COLUMN.ACTION));
                eActionType actionType = eActionType.getTypeById(action);
                int model = cursor.getInt(cursor.getColumnIndex(Log.COLUMN.MODEL));
                eModelType modelType = eModelType.getTypeId(model);
                String date = cursor.getString(cursor.getColumnIndex(Log.COLUMN.ACTION_DATE));

                list.add(new Log(id, uuid, actionType, modelType, ISO8601Utils.parse(date, new ParsePosition(0))));
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
