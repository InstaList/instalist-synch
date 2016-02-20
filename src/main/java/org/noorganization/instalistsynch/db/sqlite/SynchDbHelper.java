package org.noorganization.instalistsynch.db.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAuthAccess;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.TaskErrorLog;
import org.noorganization.instalistsynch.model.TempGroupAccessToken;
import org.noorganization.instalistsynch.model.network.ModelMapping;
import org.noorganization.instalistsynch.model.network.eModelMappingTableNames;

/**
 * Helper to access synch database.
 * Created by tinos_000 on 29.01.2016.
 */
public class SynchDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "ISSynch.db";

    public SynchDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(GroupAuthAccess.DB_CREATE);
        db.execSQL(GroupAuth.DB_CREATE);
        db.execSQL(GroupMember.DB_CREATE);
        db.execSQL(TempGroupAccessToken.DB_CREATE);
        db.execSQL(TaskErrorLog.DB_CREATE);

        for (eModelMappingTableNames modelMappingTableNames : eModelMappingTableNames.values()) {
            db.execSQL(ModelMapping.getDbCreateString(modelMappingTableNames));
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing to update.
        db.execSQL("DROP TABLE IF EXISTS " + GroupAuthAccess.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + GroupAuth.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + GroupMember.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TempGroupAccessToken.TABLE_NAME + ";");
        db.execSQL("DROP TABLE IF EXISTS " + TaskErrorLog.TABLE_NAME + ";");
        for (eModelMappingTableNames modelMappingTableNames : eModelMappingTableNames.values()) {
            db.execSQL("DROP TABLE IF EXISTS " + modelMappingTableNames.toString() + ";");
        }
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {

        // onUpgrade(db, 5, 5);

        super.onOpen(db);
    }
}
