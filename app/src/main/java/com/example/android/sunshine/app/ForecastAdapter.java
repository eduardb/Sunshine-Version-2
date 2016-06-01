package com.example.android.sunshine.app;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SingleSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.bumptech.glide.Glide;
import com.example.android.sunshine.app.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link Cursor} to a {@link RecyclerView}.
 */
public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private final Context context;
    private final OnClickHandler onClickHandler;
    private final MultiSelector selector;

    // Flag to determine if we want to use a separate view for "today".
    private boolean useTodayLayout = true;
    private Cursor cursor;

    static class ViewHolder extends SwappingHolder implements View.OnClickListener {

        final ImageView iconView;
        final TextView dateView;
        final TextView descriptionView;
        final TextView highTempView;
        final TextView lowTempView;
        private final MultiSelector selector;
        private final OnClickHandler onClickHandler;

        private long dateInMillis;

        ViewHolder(View view, MultiSelector selector, final OnClickHandler onClickHandler) {
            super(view, selector);
            this.selector = selector;
            this.onClickHandler = onClickHandler;
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            descriptionView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            highTempView = (TextView) view.findViewById(R.id.list_item_high_textview);
            lowTempView = (TextView) view.findViewById(R.id.list_item_low_textview);

            view.setOnClickListener(this);
        }

        void setDateInMillis(long dateInMillis) {
            this.dateInMillis = dateInMillis;
        }

        @Override
        public void onClick(View v) {
            onClickHandler.onClick(dateInMillis, ViewHolder.this);
            if (isSelectable()) {
                selector.setSelected(this, true);
            }
        }
    }
    public ForecastAdapter(Context context, OnClickHandler onClickHandler, boolean selectable) {
        this.context = context;
        this.onClickHandler = onClickHandler;
        selector = new SingleSelector();
        selector.setSelectable(selectable);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Choose the layout type
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }

        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        view.setFocusable(true);
        return new ViewHolder(view, selector, onClickHandler);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        cursor.moveToPosition(position);

        int viewType = holder.getItemViewType();
        int weatherId = cursor.getInt(WeatherContract.COL_WEATHER_CONDITION_ID);
        int fallbackIconId;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                fallbackIconId = Utility.getArtResourceForWeatherCondition(weatherId);
                break;
            }
            default: {
                // Get weather icon
                fallbackIconId = Utility.getIconResourceForWeatherCondition(weatherId);
                break;
            }
        }
        Glide.with(context)
                .load(Utility.getArtUrlForWeatherCondition(context, weatherId))
                .error(fallbackIconId)
                .into(holder.iconView);

        // Read date from cursor
        long dateInMillis = cursor.getLong(WeatherContract.COL_WEATHER_DATE);
        // Find TextView and set formatted date on it
        holder.dateView.setText(Utility.getFriendlyDayString(context, dateInMillis));
        holder.setDateInMillis(dateInMillis);

        // Get description from weather condition ID
        String description = Utility.getStringForWeatherCondition(context, weatherId);
        // Find TextView and set weather forecast on it
        holder.descriptionView.setText(description);
        holder.descriptionView.setContentDescription(context.getString(R.string.a11y_forecast, description));

        // For accessibility, we don't want a content description for the icon field
        // because the information is repeated in the description view and the icon
        // is not individually selectable

        // Read high temperature from cursor
        String high = Utility.formatTemperature(context, cursor.getDouble(WeatherContract.COL_WEATHER_MAX_TEMP));
        holder.highTempView.setText(high);
        holder.highTempView.setContentDescription(context.getString(R.string.a11y_high_temp, high));

        // Read low temperature from cursor
        String low = Utility.formatTemperature(context, cursor.getDouble(WeatherContract.COL_WEATHER_MIN_TEMP));
        holder.lowTempView.setText(low);
        holder.lowTempView.setContentDescription(context.getString(R.string.a11y_low_temp, low));
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        if (cursor == null) {
            return 0;
        }
        return cursor.getCount();
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    public void swapCursor(Cursor newCursor) {
        cursor = newCursor;
        notifyDataSetChanged();
    }

    public Cursor getCursor() {
        return cursor;
    }

    public interface OnClickHandler {
        void onClick(long date, ViewHolder holder);
    }
}
