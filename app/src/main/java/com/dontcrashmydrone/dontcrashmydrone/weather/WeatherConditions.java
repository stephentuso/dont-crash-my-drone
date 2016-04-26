package com.dontcrashmydrone.dontcrashmydrone.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by sunny on 11/27/15.
 */
public class WeatherConditions {

    private Long time = null;
    private Double temperature = null;
    private Double precipChance = null;
    private Double windSpeed = null;
    private String summary = null;
    private String timeZone = null;
    private String locationString = null;

    //Calculate flying
    public FlyingConditions getFlyingConditions() {
        FlyingConditions conditions = new FlyingConditions();

        if (temperature != null && temperature < 30) {
            conditions.incrementConditionIntBy(2);
            conditions.addWarning("Low temperature may cause issues");
        } else if (temperature != null && temperature > 105) {
            conditions.incrementConditionIntBy(2);
            conditions.addWarning("High temperature may cause issues");
        }

        if (windSpeed != null && windSpeed > 15) {
            conditions.incrementConditionIntBy(2);
            conditions.addWarning("Winds may be too high");
        }

        if (precipChance != null && precipChance >= 80) {
            conditions.incrementConditionIntBy(2);
            conditions.addWarning("High chance of rain");
        } else if (precipChance != null && precipChance > 50) {
            conditions.incrementConditionIntBy(1);
            conditions.addWarning("Chance of rain");
        }

        return conditions;

    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public double getHumidity() {
        return humidity;
    }

    public String getLocationAddress() {return locationString;}

    public void setLocationAddress(String locationAddress) { locationString = locationAddress;}

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    private double humidity;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTemperature() {
        return (int) Math.round(temperature);
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public int getPrecipChance() {
        double precipPercentage = precipChance * 100;
        return (int) Math.round(precipPercentage);
    }

    public void setPrecipChance(double precipChance) {
        this.precipChance = precipChance;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFormattedTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
        formatter.setTimeZone(TimeZone.getTimeZone(getTimeZone()));
        Date dateTime = new Date(getTime() * 1000);
        String timeString = formatter.format(dateTime);

        return timeString;
    }

}
