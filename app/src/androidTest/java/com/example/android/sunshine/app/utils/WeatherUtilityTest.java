package com.example.android.sunshine.app.utils;

import android.test.AndroidTestCase;

import com.example.android.sunshine.app.R;

public class WeatherUtilityTest extends AndroidTestCase {

    public void testGivenStormWeatherCondition_whenGetStringForWeatherCondition_thenReturnCorrectString() {
        int givenWeatherCondition = 200;

        String expected = getContext().getString(R.string.Storm);

        String result = WeatherUtility.getStringForWeatherCondition(getContext(), givenWeatherCondition);

        assertEquals(expected, result);
    }

    public void testGivenDrizzleWeatherCondition_whenGetStringForWeatherCondition_thenReturnCorrectString() {
        int givenWeatherCondition = 300;

        String expected = getContext().getString(R.string.Drizzle);

        String result = WeatherUtility.getStringForWeatherCondition(getContext(), givenWeatherCondition);

        assertEquals(expected, result);
    }

    public void testGivenWeatherConditionOtherThanStormOrDrizzle_whenGetStringForWeatherCondition_thenReturnCorrectString() {
        int givenWeatherCondition = 500;

        String expected = getContext().getString(R.string.Light_Rain);

        String result = WeatherUtility.getStringForWeatherCondition(getContext(), givenWeatherCondition);

        assertEquals(expected, result);
    }

    public void testGivenUnknownWeatherCondition_whenGetStringForWeatherCondition_thenReturnCorrectString() {
        int givenWeatherCondition = 999;

        String expected = getContext().getString(R.string.condition_unknown, givenWeatherCondition);

        String result = WeatherUtility.getStringForWeatherCondition(getContext(), givenWeatherCondition);

        assertEquals(expected, result);
    }

}
