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

package org.noorganization.instalistsynch.view;

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
        GlobalObjects.getInstance().initController();

        SynchDbHelper synchDbHelper = new SynchDbHelper(this);
        synchDbHelper.onUpgrade(synchDbHelper.getWritableDatabase(), 6, 6);
    }


}
