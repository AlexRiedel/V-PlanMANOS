package de.alex_riedel.v_planmanos;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Alexander on 03.02.2018.
 *
 */

public class Autostart extends BroadcastReceiver {

    public void onReceive(Context context, Intent arg1) {
        arg1.getAction();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPref.getBoolean("autoService",false)) {
            Intent intent = new Intent(context, CheckService.class);
            context.startService(intent);
            Log.i("Autostart", "started");
        }
    }
}