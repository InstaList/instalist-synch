package org.noorganization.instalistsynch.controller.local.dba.impl;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.codehaus.jackson.map.util.ISO8601Utils;
import org.noorganization.instalistsynch.controller.local.dba.IModelMappingDbController;
import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.network.ModelMapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Sqlite db access controller for ShoppingListModel Mapping
 * Created by tinos_000 on 31.01.2016.
 */
public class SqliteShoppingListMappingDbController implements IModelMappingDbController {

    private static SqliteShoppingListMappingDbController sInstance;
    private SynchDbHelper mDbHelper;
    private String mTableName;

    public static SqliteShoppingListMappingDbController getInstance(){
        if(sInstance == null)
            sInstance = new SqliteShoppingListMappingDbController();

        return sInstance;
    }

    private SqliteShoppingListMappingDbController(){
        mTableName = ModelMapping.SHOPPING_LIST_MAPPING_TABLE_NAME;
    }

    @Override
    public boolean insert(ModelMapping _element) {
        String uuid = generateUUID();
        _element.setUUID(uuid);

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues(6);
        cv.put(ModelMapping.COLUMN.ID, _element.mUUID);
        cv.put(ModelMapping.COLUMN.DEVICE_ID, _element.getDeviceId());
        cv.put(ModelMapping.COLUMN.CLIENT_SIDE_UUID, _element.getClientSideUUID());
        cv.put(ModelMapping.COLUMN.SERVER_SIDE_UUID, _element.getServerSideUUID());
        cv.put(ModelMapping.COLUMN.LAST_CLIENT_CHANGE, ISO8601Utils.format(_element.getLastClientChange()));
        cv.put(ModelMapping.COLUMN.LAST_SERVER_CHANGE, ISO8601Utils.format(_element.getLastServerChanged()));

        long rowId = db.insert(mTableName, null, cv);
        if (rowId == -1)
            return false;

        return true;
    }

    @Override
    public boolean update(ModelMapping _element) {
        if (!hasIdInDatabase(_element.getUUID()))
            return false;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues(6);
        cv.put(ModelMapping.COLUMN.ID, _element.mUUID);
        cv.put(ModelMapping.COLUMN.DEVICE_ID, _element.getDeviceId());
        cv.put(ModelMapping.COLUMN.CLIENT_SIDE_UUID, _element.getClientSideUUID());
        cv.put(ModelMapping.COLUMN.SERVER_SIDE_UUID, _element.getServerSideUUID());
        cv.put(ModelMapping.COLUMN.LAST_CLIENT_CHANGE, ISO8601Utils.format(_element.getLastClientChange()));
        cv.put(ModelMapping.COLUMN.LAST_SERVER_CHANGE, ISO8601Utils.format(_element.getLastServerChanged()));

        long updatedRows = db.update(mTableName, cv, GroupAuthAccess.COLUMN.GROUP_ID + " LIKE ?", new String[]{_element.getUUID()});
        if (updatedRows <= 0)
            return false;
        return true;
    }

    @Override
    public boolean delete(ModelMapping _element) {
        if(hasIdInDatabase(_element.getUUID()))
            return false;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.delete(mTableName, ModelMapping.COLUMN.ID + " LIKE ?", new String[]{_element.getUUID()});
        return false;
    }

    @Override
    public List<ModelMapping> get(String _whereClause, String[] _whereParams) {
        List<ModelMapping> modelMappingList = new ArrayList<>();
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor modelMappingCursor = db.query(mTableName, ModelMapping.COLUMN.ALL_COLUMNS, _whereClause, _whereParams, null, null, null);
        do {
            String uuid = modelMappingCursor.getString(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.ID));
            String deviceId = modelMappingCursor.getString(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.DEVICE_ID));
            String clientSideUUID = modelMappingCursor.getString(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.CLIENT_SIDE_UUID));
            String serverSideUUID = modelMappingCursor.getString(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.SERVER_SIDE_UUID));
            Date clientLastChanged = ISO8601Utils.parse(modelMappingCursor.getString(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.LAST_CLIENT_CHANGE)));
            Date serverLastChaged = ISO8601Utils.parse(modelMappingCursor.getString(modelMappingCursor.getColumnIndex(ModelMapping.COLUMN.LAST_SERVER_CHANGE)));

            ModelMapping modelMapping = new ModelMapping(uuid,deviceId,serverSideUUID, clientSideUUID, serverLastChaged, clientLastChanged);
            modelMappingList.add(modelMapping);
        } while (modelMappingCursor.moveToNext());
        modelMappingCursor.close();
        return modelMappingList;
    }
    /**
     * Generates a unique uuid.
     * @return a unique uuid.
     */
    private String generateUUID() {
        String uuid = null;
        do {
            uuid = UUID.randomUUID().toString();
        } while(hasIdInDatabase(uuid));
        return uuid;
    }

    private boolean hasIdInDatabase(String _uuid){
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor modelMappingCursor = db.query(mTableName, ModelMapping.COLUMN.ALL_COLUMNS, ModelMapping.COLUMN.ID + " LIKE ?", new String[]{_uuid}, null, null, null);
        boolean ret = modelMappingCursor.getCount() == 0;
        modelMappingCursor.close();
        return ret;
    }
}