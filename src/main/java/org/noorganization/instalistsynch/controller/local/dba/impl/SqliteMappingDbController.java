package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.codehaus.jackson.map.util.ISO8601Utils;
import org.noorganization.instalist.utils.SQLiteUtils;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.controller.local.dba.exception.SqliteMappingDbControllerException;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.model.eModelMappingTableNames;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Sqlite db access controller for ShoppingListModel Mapping
 * Created by tinos_000 on 31.01.2016.
 */
public class SqliteMappingDbController implements IModelMappingDbController {

    private static SqliteMappingDbController sInstance;
    private SynchDbHelper mDbHelper;
    private String mTableName;

    public SqliteMappingDbController(eModelMappingTableNames _tableName, Context _context) throws
            SqliteMappingDbControllerException {
        mTableName = _tableName.toString();
        mDbHelper = new SynchDbHelper(_context);

    }


    @Override
    public String generateUuid() {
        return SQLiteUtils.generateId(mDbHelper.getReadableDatabase(),
                mTableName,
                ModelMapping.COLUMN.SERVER_SIDE_UUID)
                .toString();
    }

    @Override
    public ModelMapping insert(ModelMapping _element) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        String uuid = SQLiteUtils.generateId(db, mTableName, ModelMapping.COLUMN.ID)
                .toString();
        _element.setUUID(uuid);
        _element.setUUID(uuid);
        ContentValues cv = new ContentValues(6);
        cv.put(ModelMapping.COLUMN.ID, _element.mUUID);
        cv.put(ModelMapping.COLUMN.GROUP_ID, _element.getGroupId());
        cv.put(ModelMapping.COLUMN.CLIENT_SIDE_UUID, _element.getClientSideUUID());
        cv.put(ModelMapping.COLUMN.SERVER_SIDE_UUID, _element.getServerSideUUID());
        cv.put(ModelMapping.COLUMN.LAST_CLIENT_CHANGE,
                ISO8601Utils.format(_element.getLastClientChange()));
        cv.put(ModelMapping.COLUMN.LAST_SERVER_CHANGE,
                ISO8601Utils.format(_element.getLastServerChanged()));
        cv.put(ModelMapping.COLUMN.DELETED, _element.isDeleted());
        long rowId = db.insert(mTableName, null, cv);
        if (rowId == -1) {
            return null;
        }

        return _element;
    }

    @Override
    public boolean update(ModelMapping _element) {
        if (!hasIdInDatabase(_element.getUUID())) {
            return false;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues  cv = new ContentValues(6);
        cv.put(ModelMapping.COLUMN.ID, _element.mUUID);
        cv.put(ModelMapping.COLUMN.GROUP_ID, _element.getGroupId());
        cv.put(ModelMapping.COLUMN.CLIENT_SIDE_UUID, _element.getClientSideUUID());
        cv.put(ModelMapping.COLUMN.SERVER_SIDE_UUID, _element.getServerSideUUID());
        cv.put(ModelMapping.COLUMN.LAST_CLIENT_CHANGE,
                ISO8601Utils.format(_element.getLastClientChange()));
        cv.put(ModelMapping.COLUMN.LAST_SERVER_CHANGE,
                ISO8601Utils.format(_element.getLastServerChanged()));
        cv.put(ModelMapping.COLUMN.DELETED, _element.isDeleted());

        long updatedRows = db.update(mTableName,
                cv,
                GroupAccess.COLUMN.GROUP_ID + " LIKE ?",
                new String[]{_element.getUUID()});
        if (updatedRows <= 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean delete(ModelMapping _element) {
        if (hasIdInDatabase(_element.getUUID())) {
            return false;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(mTableName, ModelMapping.COLUMN.ID + " LIKE ?", new String[]{_element.getUUID()});
        return false;
    }

    @Override
    public List<ModelMapping> get(String _whereClause, String[] _whereParams) {
        List<ModelMapping> modelMappingList = new ArrayList<>();
        SQLiteDatabase     db               = mDbHelper.getReadableDatabase();
        Cursor modelMappingCursor = db.query(mTableName,
                ModelMapping.COLUMN.ALL_COLUMNS,
                _whereClause,
                _whereParams,
                null,
                null,
                null);

        if (modelMappingCursor.getCount() == 0) {
            modelMappingCursor.close();
            return modelMappingList;
        }

        modelMappingCursor.moveToFirst();

        do {
            String uuid =
                    modelMappingCursor.getString(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.ID));
            int deviceId =
                    modelMappingCursor.getInt(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.GROUP_ID));
            String clientSideUUID =
                    modelMappingCursor.getString(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.CLIENT_SIDE_UUID));
            String serverSideUUID =
                    modelMappingCursor.getString(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.SERVER_SIDE_UUID));
            Date clientLastChanged =
                    ISO8601Utils.parse(modelMappingCursor.getString(modelMappingCursor.getColumnIndex(
                            ModelMapping.COLUMN.LAST_CLIENT_CHANGE)));
            Date serverLastChaged =
                    ISO8601Utils.parse(modelMappingCursor.getString(modelMappingCursor.getColumnIndex(
                            ModelMapping.COLUMN.LAST_SERVER_CHANGE)));
            boolean deleted = modelMappingCursor.getInt(modelMappingCursor.getColumnIndex(
                    ModelMapping.COLUMN.DELETED)) == 1;

            ModelMapping modelMapping = new ModelMapping(uuid,
                    deviceId,
                    serverSideUUID,
                    clientSideUUID,
                    serverLastChaged,
                    clientLastChanged, deleted);
            modelMappingList.add(modelMapping);
        }
        while (modelMappingCursor.moveToNext());
        modelMappingCursor.close();
        return modelMappingList;
    }

    private boolean hasIdInDatabase(String _uuid) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor modelMappingCursor = db.query(mTableName,
                ModelMapping.COLUMN.ALL_COLUMNS,
                ModelMapping.COLUMN.ID + " LIKE ?",
                new String[]{_uuid},
                null,
                null,
                null);
        boolean ret = modelMappingCursor.getCount() == 0;
        modelMappingCursor.close();
        return ret;
    }
}
