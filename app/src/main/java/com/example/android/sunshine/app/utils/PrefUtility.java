package com.example.android.sunshine.app.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.WorkerThread;

import com.example.android.sunshine.app.R;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

public class PrefUtility {

    private static final float DEFAULT_LATLONG = 0F;

    private PrefUtility() throws IllegalAccessException {
        throw new IllegalAccessException("Class shouldn't ne instantiated!");
    }

    public static boolean isLocationLatLonAvailable(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.contains(context.getString(R.string.pref_location_latitude))
                && prefs.contains(context.getString(R.string.pref_location_longitude));
    }

    public static float getLocationLatitude(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getFloat(
                context.getString(R.string.pref_location_latitude),
                DEFAULT_LATLONG
        );
    }

    public static float getLocationLongitude(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getFloat(
                context.getString(R.string.pref_location_longitude),
                DEFAULT_LATLONG
        );
    }

    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_location_key),
                context.getString(R.string.pref_location_default)
        );
    }

    static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                context.getString(R.string.pref_units_key),
                context.getString(R.string.pref_units_metric)
        )
                .equals(context.getString(R.string.pref_units_metric));
    }

    public static String formatTemperature(Context context, double temperature) {
        // Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
        // the values here.
        if (!isMetric(context)) {
            temperature = (temperature * 1.8) + 32;
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        return String.format(context.getString(R.string.format_temperature), temperature);
    }

    @SunshineSyncAdapter.LocationStatus
    public static int getLocationStatus(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //noinspection WrongConstant
        return preferences.getInt(context.getString(R.string.pref_location_status_key), SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }

    /**
     * Sets the location status into shared preference. This function should not be called from the UI thread
     * because it uses commit to write to the shared preferences
     *
     * @param context        Context to get the PreferenceManager from
     * @param locationStatus The IntDev value to set
     */
    @SuppressLint("CommitPrefEdits")
    @WorkerThread
    public static void setLocationStatus(Context context, @SunshineSyncAdapter.LocationStatus int locationStatus) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(context.getString(R.string.pref_location_status_key), locationStatus);
        editor.commit();
    }

    public static void resetLocationStatus(Context context) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putInt(context.getString(R.string.pref_location_status_key), SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
        editor.apply();
    }
}
