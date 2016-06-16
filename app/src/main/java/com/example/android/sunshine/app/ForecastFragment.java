/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.example.android.sunshine.app.data.WeatherContract;
import com.example.android.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Encapsulates fetching the forecast and displaying it as a {@link RecyclerView} layout.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ForecastAdapter forecastAdapter;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private int position = RecyclerView.NO_POSITION;
    private boolean useTodayLayout;
    private boolean autoSelectView;
    private boolean holdForTransition;

    private static final String SELECTED_KEY = "selected_position";

    private static final int FORECAST_LOADER = 0;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri, ForecastAdapter.ViewHolder viewHolder);
    }

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ForecastFragment);
        autoSelectView = typedArray.getBoolean(R.styleable.ForecastFragment_autoSelectView, false);
        holdForTransition = typedArray.getBoolean(R.styleable.ForecastFragment_sharedElementTransitions, false);
        typedArray.recycle();
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getContext()).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_refresh) {
//            updateWeather();
//            return true;
//        }
        if (id == R.id.action_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        emptyView = (TextView) rootView.findViewById(R.id.recyclerview_forecast_empty);

        // The ForecastAdapter will take data from a source and
        // use it to populate the RecyclerView it's attached to.
        forecastAdapter = new ForecastAdapter(getContext(), onClickHandler, autoSelectView);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_forecast);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setAdapter(forecastAdapter);

        View parallaxView = rootView.findViewById(R.id.parallax_bar);
        setUpParallax(parallaxView);

        AppBarLayout appBar = (AppBarLayout) rootView.findViewById(R.id.appbar);
        setUpElevation(appBar);

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            position = savedInstanceState.getInt(SELECTED_KEY);
            forecastAdapter.setSelection(position);
        }

        forecastAdapter.setUseTodayLayout(useTodayLayout);

        return rootView;
    }

    @Override
    public void onDestroyView() {
        recyclerView.clearOnScrollListeners();
        super.onDestroyView();
    }

    private ForecastAdapter.OnClickHandler onClickHandler = new ForecastAdapter.OnClickHandler() {
        @Override
        public void onClick(long date, ForecastAdapter.ViewHolder holder) {
            String locationSetting = Utility.getPreferredLocation(getActivity());
            ((Callback) getActivity()).onItemSelected(
                    WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, date),
                    holder
            );
            ForecastFragment.this.position = holder.getAdapterPosition();
        }
    };

    private void setUpParallax(final View parallaxView) {
        if (parallaxView == null) {
            return;
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int max = parallaxView.getHeight();
                if (dy > 0) {
                    parallaxView.setTranslationY(Math.max(-max, parallaxView.getTranslationY() - dy / 2));
                } else {
                    parallaxView.setTranslationY(Math.min(0, parallaxView.getTranslationY() - dy / 2));
                }
            }
        });
    }

    private void setUpElevation(final AppBarLayout appBar) {
        if (appBar == null) {
            return;
        }

        ViewCompat.setElevation(appBar, 0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (recyclerView.computeVerticalScrollOffset() == 0) {
                    appBar.setElevation(0);
                } else {
                    appBar.setElevation(appBar.getTargetElevation());
                }
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // We hold for transition here just in case the activity
        // needs to be recreated. In a standard return transition,
        // this doesn't actually make a difference
        if (holdForTransition) {
            getActivity().supportPostponeEnterTransition();
        }
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
        // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if (null != forecastAdapter) {
            Cursor c = forecastAdapter.getCursor();
            if (null != c) {
                c.moveToPosition(0);
                String posLat = c.getString(WeatherContract.COL_COORD_LAT);
                String posLong = c.getString(WeatherContract.COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }

        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, position will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (position != RecyclerView.NO_POSITION) {
            outState.putInt(SELECTED_KEY, position);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, filter the query to return weather only for
        // dates after or including today.

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";

        String locationSetting = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, System.currentTimeMillis());

        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                WeatherContract.FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        forecastAdapter.swapCursor(data);
        if (position != RecyclerView.NO_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            recyclerView.smoothScrollToPosition(position);
        }
        updateEmptyView();
        if (data.getCount() == 0) {
            getActivity().supportStartPostponedEnterTransition();
        } else {
            updateSelection();
        }
    }

    private void updateSelection() {
        recyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                // Since we know we're going to get items, we keep the listener around until
                // we see Children.
                if (recyclerView.getChildCount() > 0) {
                    recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                    int itemPosition = forecastAdapter.getSelectedItemPosition();
                    if (RecyclerView.NO_POSITION == itemPosition) {
                        itemPosition = 0;
                    }
                    RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(itemPosition);
                    if (null != vh && autoSelectView) {
                        forecastAdapter.selectView(vh);
                    }
                    if (holdForTransition) {
                        getActivity().supportStartPostponedEnterTransition();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        forecastAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
        if (forecastAdapter != null) {
            forecastAdapter.setUseTodayLayout(this.useTodayLayout);
        }
    }

    /**
     * Updates the empty list view with contextually relevant information that the user can
     * use to determine why they aren't seeing weather
     */
    private void updateEmptyView() {
        if (forecastAdapter.getItemCount() == 0) {
            @StringRes
            int message = R.string.empty_forecast_list;
            switch (Utility.getLocationStatus(getContext())) {
                case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                    message = R.string.empty_forecast_list_server_down;
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                    message = R.string.empty_forecast_list_server_error;
                    break;
                case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                    message = R.string.empty_forecast_list_invalid_location;
                    break;
                default:
                    if (!Utility.isNetworkAvailable(getContext())) {
                        message = R.string.empty_forecast_list_no_network;
                    }
            }
            emptyView.setText(message);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!getString(R.string.pref_location_status_key).equals(key)) {
            return;
        }
        updateEmptyView();
    }
}
