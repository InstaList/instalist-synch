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