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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.noorganization.instalistsynch.service.SyncService;

/**
 * A receiver that is notified when the device is started.
 * Created by ts
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context _Context, Intent _Intent) {
        if (_Intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            // set the alarm.
            //boolean startNewsService = PreferenceManager.getDefaultSharedPreferences(_Context).getBoolean(Preferences.PREF_ENABLE_NEWS_NOTIFICATION, false);
            boolean startNewsService = true;

            // only start if it is enabled!
            if (startNewsService) {
                Intent service = new Intent(_Context, SyncService.class);
                _Context.startService(service);
                //int updateRate = PreferenceManager.getDefaultSharedPreferences(_Context).getInt(Preferences.UPDATE_RATE, Preferences.MIN_UPDATE_RATE);
                //AlertHelper alertHelper = new AlertHelper();
                //alertHelper.startNewsAlarmManager(_Context, updateRate);
            }
        }
    }
}