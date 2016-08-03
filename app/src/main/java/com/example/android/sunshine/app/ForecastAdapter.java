package com.example.android.sunshine.app;

import android.content.Context;
import android.support.v4.view.ViewCompat;
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
import com.example.android.sunshine.app.data.WeatherConditions;
import com.example.android.sunshine.app.utils.DateUtility;
import com.example.android.sunshine.app.utils.PrefUtility;
import com.example.android.sunshine.app.utils.WeatherUtility;

import java.util.ArrayList;
import java.util.List;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ViewHolder> {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private final Context context;
    private final OnClickHandler onClickHandler;
    private final MultiSelector selector;

    // Flag to determine if we want to use a separate view for "today".
    private boolean useTodayLayout = true;

    private final List<WeatherConditions> weatherConditionsList = new ArrayList<>(14);

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
        WeatherConditions conditions = weatherConditionsList.get(position);

        int viewType = holder.getItemViewType();
        int weatherId = conditions.weatherId();
        int fallbackIconId;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                // Get weather icon
                fallbackIconId = WeatherUtility.getArtResourceForWeatherCondition(weatherId);
                break;
            }
            default: {
                // Get weather icon
                fallbackIconId = WeatherUtility.getIconResourceForWeatherCondition(weatherId);
                break;
            }
        }
        Glide.with(context)
                .load(WeatherUtility.getArtUrlForWeatherCondition(context, weatherId))
                .error(fallbackIconId)
                .into(holder.iconView);

        // This enables better animations. Even if we lose state due to a device rotation,
        // the animator can use this to re-find the original view
        ViewCompat.setTransitionName(holder.iconView, "iconView" + position);

        long dateInMillis = conditions.date();
        // Find TextView and set formatted date on it
        holder.dateView.setText(DateUtility.getFriendlyDayString(context, dateInMillis));
        holder.setDateInMillis(dateInMillis);

        // Get description from weather condition ID
        String description = WeatherUtility.getStringForWeatherCondition(context, weatherId);
        // Find TextView and set weather forecast on it
        holder.descriptionView.setText(description);
        holder.descriptionView.setContentDescription(context.getString(R.string.a11y_forecast, description));

        // For accessibility, we don't want a content description for the icon field
        // because the information is repeated in the description view and the icon
        // is not individually selectable

        String high = PrefUtility.formatTemperature(context, conditions.maxTemp());
        holder.highTempView.setText(high);
        holder.highTempView.setContentDescription(context.getString(R.string.a11y_high_temp, high));

        String low = PrefUtility.formatTemperature(context, conditions.minTemp());
        holder.lowTempView.setText(low);
        holder.lowTempView.setContentDescription(context.getString(R.string.a11y_low_temp, low));
    }

    public WeatherConditions getItemAt(int position) {
        return weatherConditionsList.get(position);
    }

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
            onClickHandler.onClick(getAdapterPosition(), dateInMillis, iconView);
            if (isSelectable()) {
                selector.setSelected(this, true);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0 && useTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getItemCount() {
        return weatherConditionsList.size();
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        this.useTodayLayout = useTodayLayout;
    }

    public void setData(List<WeatherConditions> forecast) {
        weatherConditionsList.clear();
        weatherConditionsList.addAll(forecast);
        notifyDataSetChanged();
    }

    public void setSelection(int position) {
        if (selector.isSelectable()) {
            selector.setSelected(position, getItemId(position), true);
        }
    }

    public int getSelectedItemPosition() {
        List<Integer> selectedPositions = selector.getSelectedPositions();
        if (selectedPositions.isEmpty()) {
            return RecyclerView.NO_POSITION;
        }
        return selectedPositions.get(0);
    }

    public void selectView(RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof ViewHolder) {
            ((ViewHolder) viewHolder).onClick(viewHolder.itemView);
        }
    }


    public interface OnClickHandler {
        void onClick(int position, long date, View sharedElement);
    }
}
