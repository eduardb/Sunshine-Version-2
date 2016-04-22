package com.example.android.sunshine.app;

import android.test.AndroidTestCase;

public class UtilityTest extends AndroidTestCase {

    public void testGivenStormWeatherCondition_whenGetStringForWeatherCondition_thenReturnCorrectString() {
        int givenWeatherCondition = 200;

        String expected = getContext().getString(R.string.Storm);

        String result = Utility.getStringForWeatherCondition(getContext(), givenWeatherCondition);

        assertEquals(expected, result);
    }

    public void testGivenDrizzleWeatherCondition_whenGetStringForWeatherCondition_thenReturnCorrectString() {
        int givenWeatherCondition = 300;

        String expected = getContext().getString(R.string.Drizzle);

        String result = Utility.getStringForWeatherCondition(getContext(), givenWeatherCondition);

        assertEquals(expected, result);
    }

    public void testGivenWeatherConditionOtherThanStormOrDrizzle_whenGetStringForWeatherCondition_thenReturnCorrectString() {
        int givenWeatherCondition = 500;

        String expected = getContext().getString(R.string.Light_Rain);

        String result = Utility.getStringForWeatherCondition(getContext(), givenWeatherCondition);

        assertEquals(expected, result);
    }

    public void testGivenUnknownWeatherCondition_whenGetStringForWeatherCondition_thenReturnCorrectString() {
        int givenWeatherCondition = 999;

        String expected = getContext().getString(R.string.condition_unknown, givenWeatherCondition);

        String result = Utility.getStringForWeatherCondition(getContext(), givenWeatherCondition);

        assertEquals(expected, result);
    }

}
