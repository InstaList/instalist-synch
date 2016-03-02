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

package org.noorganization.instalistsynch.db.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.noorganization.instalistsynch.model.GroupAuth;
import org.noorganization.instalistsynch.model.GroupAccess;
import org.noorganization.instalistsynch.model.GroupMember;
import org.noorganization.instalistsynch.model.TaskErrorLog;
import org.noorganization.instalistsynch.model.TempGroupAccessToken;
import org.noorganization.instalistsynch.model.ModelMapping;
import org.noorganization.instalistsynch.model.eModelMappingTableNames;

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
        db.execSQL(GroupAccess.DB_CREATE);
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
//        db.execSQL("DROP TABLE IF EXISTS " + GroupAccess.TABLE_NAME + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + GroupAuth.TABLE_NAME + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + GroupMember.TABLE_NAME + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + TempGroupAccessToken.TABLE_NAME + ";");
//        db.execSQL("DROP TABLE IF EXISTS " + TaskErrorLog.TABLE_NAME + ";");
//        for (eModelMappingTableNames modelMappingTableNames : eModelMappingTableNames.values()) {
//            db.execSQL("DROP TABLE IF EXISTS " + modelMappingTableNames.toString() + ";");
//        }
//        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {

        // onUpgrade(db, 5, 5);

        super.onOpen(db);
    }
}
