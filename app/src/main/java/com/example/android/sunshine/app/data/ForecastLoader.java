package com.example.android.sunshine.app.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.android.sunshine.app.utils.PrefUtility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForecastLoader {

    private static final int FORECAST_LOADER = 0;

    final Context context;
    final LoaderManager loaderManager;
    final ForecastLoaderListener forecastLoaderListener;
    private final LoaderCallbacks loaderCallbacks;

    public ForecastLoader(Context context, LoaderManager loaderManager, ForecastLoaderListener forecastLoaderListener) {
        this.context = context;
        this.loaderManager = loaderManager;
        this.forecastLoaderListener = forecastLoaderListener;
        this.loaderCallbacks = new LoaderCallbacks();
    }

    public void startLoading() {
        loaderManager.initLoader(FORECAST_LOADER, null, loaderCallbacks);
    }

    public void restartLoading() {
        loaderManager.restartLoader(FORECAST_LOADER, null, loaderCallbacks);
    }

    public interface ForecastLoaderListener {
        void onForecastLoaded(List<WeatherConditions> forecast);
    }

    private class LoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {

        // For the forecast view we're showing only a small subset of the stored data.
        // Specify the columns we need.
        private final String[] FORECAST_COLUMNS = {
                // In this case the id needs to be fully qualified with a table name, since
                // the content provider joins the location & weather tables in the background
                // (both have an _id column)
                // On the one hand, that's annoying.  On the other, you can search the weather table
                // using the location set by the user, which is only in the Location table.
                // So the convenience is worth it.
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG
        };
        // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        private static final int COL_WEATHER_ID = 0;
        private static final int COL_WEATHER_DATE = 1;
        private static final int COL_WEATHER_DESC = 2;
        private static final int COL_WEATHER_MAX_TEMP = 3;
        private static final int COL_WEATHER_MIN_TEMP = 4;
        private static final int COL_LOCATION_SETTING = 5;
        private static final int COL_WEATHER_CONDITION_ID = 6;
        private static final int COL_COORD_LAT = 7;
        private static final int COL_COORD_LONG = 8;

        LoaderCallbacks() {
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

            String locationSetting = PrefUtility.getPreferredLocation(context);
            Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                    locationSetting, System.currentTimeMillis());

            return new CursorLoader(
                    context,
                    weatherForLocationUri,
                    FORECAST_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (forecastLoaderListener == null) {
                return;
            }

            List<WeatherConditions> forecast = new ArrayList<>();
            while (data != null && data.moveToNext()) {
                WeatherConditions weatherConditions = new AutoValue_WeatherConditions(
                        data.getInt(COL_WEATHER_CONDITION_ID),
                        data.getLong(COL_WEATHER_DATE),
                        data.getDouble(COL_WEATHER_MAX_TEMP),
                        data.getDouble(COL_WEATHER_MIN_TEMP),
                        0f, 0f, 0f, 0f, // not used,
                        data.getString(COL_COORD_LAT),
                        data.getString(COL_COORD_LONG)
                );
                forecast.add(weatherConditions);
            }

            forecastLoaderListener.onForecastLoaded(Collections.unmodifiableList(forecast));
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (forecastLoaderListener != null) {
                forecastLoaderListener.onForecastLoaded(Collections.<WeatherConditions>emptyList());
            }
        }
    }
}
