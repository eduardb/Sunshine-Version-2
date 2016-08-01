package com.example.android.sunshine.app.data;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class WeatherConditions {

    public abstract int weatherId();

    public abstract long date();

    public abstract double maxTemp();

    public abstract double minTemp();

    public abstract float humidity();

    public abstract float windSpeed();

    public abstract float windDirection();

    public abstract float pressure();


}
