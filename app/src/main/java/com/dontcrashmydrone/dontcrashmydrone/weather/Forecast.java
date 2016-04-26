package com.dontcrashmydrone.dontcrashmydrone.weather;

/**
 * Created by sunny on 4/23/16.
 * Not currently used anywhere, may be added
 */

public class Forecast {

    private WeatherConditions mCurrent;

    public WeatherConditions getCurrent() {
        return mCurrent;
    }

    public void setCurrent(WeatherConditions current) {
        mCurrent = current;
    }
}