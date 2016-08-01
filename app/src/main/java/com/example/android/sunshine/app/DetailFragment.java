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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.android.sunshine.app.data.WeatherConditions;
import com.example.android.sunshine.app.data.WeatherDetailsLoader;
import com.example.android.sunshine.app.data.WeatherDetailsLoader.OnWeatherDetailsLoadedListener;
import com.example.android.sunshine.app.utils.DateUtility;
import com.example.android.sunshine.app.utils.PrefUtility;
import com.example.android.sunshine.app.utils.WeatherUtility;

public class DetailFragment extends Fragment {

    static final String DETAIL_URI = "URI";
    static final String DETAIL_TRANSITION_ANIMATION = "DETAIL_TRANSITION_ANIMATION";

    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";

    private String forecast;
    private boolean transitionAnimation;

    private ImageView iconView;
    private TextView dateView;
    private TextView descriptionView;
    private TextView highTempView;
    private TextView lowTempView;
    private TextView humidityView;
    private TextView humidityLabelView;
    private TextView windView;
    private TextView windLabelView;
    private TextView pressureView;
    private TextView pressureLabelView;

    private WeatherDetailsLoader weatherDetailsLoader;

    public DetailFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Uri uri = null;
        Bundle arguments = getArguments();
        if (arguments != null) {
            uri = arguments.getParcelable(DETAIL_URI);
            transitionAnimation = arguments.getBoolean(DETAIL_TRANSITION_ANIMATION);
        }
        weatherDetailsLoader = new WeatherDetailsLoader(uri, getContext(), getLoaderManager(), onWeatherDetailsLoadedListener);

        View rootView = inflater.inflate(R.layout.fragment_detail_start, container, false);
        iconView = (ImageView) rootView.findViewById(R.id.detail_icon);
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        highTempView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowTempView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        humidityView = (TextView) rootView.findViewById(R.id.detail_humidity_textview);
        humidityLabelView = (TextView) rootView.findViewById(R.id.detail_humidity_label_textview);
        windView = (TextView) rootView.findViewById(R.id.detail_wind_textview);
        windLabelView = (TextView) rootView.findViewById(R.id.detail_wind_label_textview);
        pressureView = (TextView) rootView.findViewById(R.id.detail_pressure_textview);
        pressureLabelView = (TextView) rootView.findViewById(R.id.detail_pressure_label_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (transitionAnimation) {
            // Inflate the menu; this adds items to the action bar if it is present.
            inflater.inflate(R.menu.detailfragment, menu);
            finishCreatingMenu(menu);
        }
    }

    private void finishCreatingMenu(Menu menu) {
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareForecastIntent());
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, forecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        weatherDetailsLoader.startLoading();
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged(String newLocation) {
        weatherDetailsLoader.restartLoadingFor(newLocation);
    }

    private OnWeatherDetailsLoadedListener onWeatherDetailsLoadedListener = new OnWeatherDetailsLoadedListener() {
        @Override
        public void onWeatherDetailsLoaded(WeatherConditions weatherConditions) {
            if (weatherConditions != null) {
                updateUiWith(weatherConditions);
            }

            setUpToolbar();
        }
    };

    void updateUiWith(WeatherConditions conditions) {
        int weatherId = conditions.weatherId();

        // Use weather art image
        Glide.with(this)
                .load(WeatherUtility.getArtUrlForWeatherCondition(getContext(), weatherId))
                .error(WeatherUtility.getArtResourceForWeatherCondition(weatherId))
                .into(iconView);

        String dateText = DateUtility.getFullFriendlyDayString(getActivity(), conditions.date());
        dateView.setText(dateText);

        // Get description from weather condition ID
        String description = WeatherUtility.getStringForWeatherCondition(getActivity(), weatherId);
        descriptionView.setText(description);
        descriptionView.setContentDescription(getString(R.string.a11y_forecast, description));

        // For accessibility, add a content description to the icon field. Because the ImageView
        // is independently focusable, it's better to have a description of the image. Using
        // null is appropriate when the image is purely decorative or when the image already
        // has text describing it in the same UI component.
        iconView.setContentDescription(getString(R.string.a11y_forecast_icon, description));

        String highString = PrefUtility.formatTemperature(getActivity(), conditions.maxTemp());
        highTempView.setText(highString);
        highTempView.setContentDescription(getString(R.string.a11y_high_temp, highString));

        String lowString = PrefUtility.formatTemperature(getActivity(), conditions.minTemp());
        lowTempView.setText(lowString);
        lowTempView.setContentDescription(getString(R.string.a11y_low_temp, lowString));

        humidityView.setText(getActivity().getString(R.string.format_humidity, conditions.humidity()));
        humidityView.setContentDescription(getString(R.string.a11y_humidity, humidityView.getText()));
        humidityLabelView.setContentDescription(humidityView.getContentDescription());

        windView.setText(WeatherUtility.getFormattedWind(getActivity(), conditions.windSpeed(), conditions.windDirection()));
        windView.setContentDescription(getString(R.string.a11y_wind, windView.getText()));
        windLabelView.setContentDescription(windView.getContentDescription());

        pressureView.setText(getString(R.string.format_pressure, conditions.pressure()));
        pressureView.setContentDescription(getString(R.string.a11y_pressure, pressureView.getText()));
        pressureLabelView.setContentDescription(pressureView.getContentDescription());

        // We still need this for the share intent
        forecast = String.format("%s - %s - %s/%s", dateText, description, conditions.maxTemp(), conditions.minTemp());
    }

    void setUpToolbar() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        Toolbar toolbarView = (Toolbar) getView().findViewById(R.id.toolbar);

        // We need to start the enter transition after the data has loaded
        if (transitionAnimation) {
            activity.supportStartPostponedEnterTransition();

            if (null != toolbarView) {
                activity.setSupportActionBar(toolbarView);

                ActionBar actionBar = activity.getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayShowTitleEnabled(false);
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
        } else {
            if (null != toolbarView) {
                Menu menu = toolbarView.getMenu();
                if (null != menu){
                    menu.clear();
                }
                toolbarView.inflateMenu(R.menu.detailfragment);
                finishCreatingMenu(toolbarView.getMenu());
            }
        }
    }

}
