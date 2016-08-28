package com.example.android.sunshine.app.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

public class WeatherDetailsLoader {

    private static final int DETAIL_LOADER = 0;

    Uri uri;
    final Context context;
    final LoaderManager loaderManager;
    final WeatherDetailsLoaderListener weatherDetailsLoaderListener;
    private final LoaderCallbacks loaderCallbacks;

    public WeatherDetailsLoader(Uri uri, Context context, LoaderManager loaderManager, WeatherDetailsLoaderListener weatherDetailsLoaderListener) {
        this.uri = uri;
        this.context = context;
        this.loaderManager = loaderManager;
        this.weatherDetailsLoaderListener = weatherDetailsLoaderListener;
        this.loaderCallbacks = new LoaderCallbacks();
    }

    public void startLoading() {
        loaderManager.initLoader(DETAIL_LOADER, null, loaderCallbacks);
    }

    public void restartLoadingFor(String newLocation) {
        if (null == uri) {
            return;
        }
        // replace the uri, since the location has changed
        long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
        uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
        loaderManager.restartLoader(DETAIL_LOADER, null, loaderCallbacks);
    }

    public interface WeatherDetailsLoaderListener {

        void onWeatherDetailsLoaded(WeatherConditions weatherConditions);

        void onLoadCompleted();

    }

    private class LoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        private final String[] DETAIL_COLUMNS = {
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
                WeatherContract.WeatherEntry.COLUMN_PRESSURE,
                WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
                WeatherContract.WeatherEntry.COLUMN_DEGREES,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                // This works because the WeatherProvider returns location data joined with
                // weather data, even though they're stored in two different tables.
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
        };
        // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
        // must change.
        private static final int COL_WEATHER_ID = 0;
        private static final int COL_WEATHER_DATE = 1;
        private static final int COL_WEATHER_DESC = 2;
        private static final int COL_WEATHER_MAX_TEMP = 3;
        private static final int COL_WEATHER_MIN_TEMP = 4;
        private static final int COL_WEATHER_HUMIDITY = 5;
        private static final int COL_WEATHER_PRESSURE = 6;
        private static final int COL_WEATHER_WIND_SPEED = 7;
        private static final int COL_WEATHER_DEGREES = 8;
        private static final int COL_WEATHER_CONDITION_ID = 9;

        LoaderCallbacks() {
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (null != uri) {
                // Now create and return a CursorLoader that will take care of
                // creating a Cursor for the data being displayed.
                return new CursorLoader(
                        context,
                        uri,
                        DETAIL_COLUMNS,
                        null,
                        null,
                        null
                );
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (weatherDetailsLoaderListener == null) {
                return;
            }

            if (data != null && data.moveToFirst()) {
                WeatherConditions weatherConditions = new AutoValue_WeatherConditions(
                        data.getInt(COL_WEATHER_CONDITION_ID),
                        data.getLong(COL_WEATHER_DATE),
                        data.getDouble(COL_WEATHER_MAX_TEMP),
                        data.getDouble(COL_WEATHER_MIN_TEMP),
                        data.getFloat(COL_WEATHER_HUMIDITY),
                        data.getFloat(COL_WEATHER_WIND_SPEED),
                        data.getFloat(COL_WEATHER_DEGREES),
                        data.getFloat(COL_WEATHER_PRESSURE),
                        null, null // unused
                );
                weatherDetailsLoaderListener.onWeatherDetailsLoaded(weatherConditions);
            }

            weatherDetailsLoaderListener.onLoadCompleted();

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }
}
