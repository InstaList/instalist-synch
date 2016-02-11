package org.noorganization.instalistsynch;

import org.noorganization.instalistsynch.db.sqlite.SynchDbHelper;
import org.noorganization.instalistsynch.utils.GlobalObjects;

/**
 * The main application, entry point of the app.
 * Created by tinos_000 on 29.01.2016.
 */
public class MainApplication extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalObjects.getInstance().setApplicationContext(this.getApplicationContext());

        SynchDbHelper synchDbHelper = new SynchDbHelper(this);
        synchDbHelper.onUpgrade(synchDbHelper.getWritableDatabase(), 6, 6);
    }


}
