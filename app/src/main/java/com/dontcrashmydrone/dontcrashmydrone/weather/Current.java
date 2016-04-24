package com.dontcrashmydrone.dontcrashmydrone.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by sunny on 11/27/15.
 */
public class Current {
    private long mTime;
    private double mTemperature;
    private double mPrecipChance;
    private String mSummary;
    private String mTimeZone;
    private String mLocationAddress;

    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;
    }

    public double getHumidity() {
        return mHumidity;
    }

    public String getLocationAddress() {return mLocationAddress;}

    public void setLocationAddress(String LocationAddress) { mLocationAddress = LocationAddress;}

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }

    private double mHumidity;

    public long getTime() {
        return mTime;
    }

    public void setTime(long time) {
        mTime = time;
    }

    public int getTemperature() {
        return (int) Math.round(mTemperature);
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public int getPrecipChance() {
        double precipPercentage = mPrecipChance * 100;
        return (int) Math.round(precipPercentage);
    }

    public void setPrecipChance(double precipChance) {
        mPrecipChance = precipChance;
    }

    public String getSummary() {
        return mSummary;
    }

    public void setSummary(String summary) {
        mSummary = summary;
    }

    public String getFormattedTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        Date dateTime = new Date(getTime() * 1000);
        String timeString = formatter.format(dateTime);

        return timeString;
    }
}
