package com.lordnoisy.hoobabot.weather;

public class Forecast {
    private final String forecastDate;
    private final String forecastStartHour;
    private final String forecastDuration;
    private final String feelsLikeDayTemperature;
    private final String feelsLikeNightTemperature;
    private final String dayMaximumTemperature;
    private final String nightMaximumTemperature;
    private final String windGustNoon;
    private final String windGustMidnight;
    private final String screenRelativeHumidityNoon;
    private final String getScreenRelativeHumidityMidnight;
    private final String visibility;
    private final String visibilityNight;
    private final String windDirectionDay;
    private final String windDirectionNight;
    private final String windSpeedMPH;
    private final String windSpeedMPHNight;
    private final String maxUVIndex;
    private final String weatherType;
    private final String weatherTypeNight;
    private final String precipitationProbabilityDay;
    private final String getPrecipitationProbabilityNight;

    public Forecast(String forecastDate, String forecastStartHour, String forecastDuration, String feelsLikeDayTemperature, String feelsLikeNightTemperature, String dayMaximumTemperature, String nightMaximumTemperature, String windGustNoon, String windGustMidnight, String screenRelativeHumidityNoon, String getScreenRelativeHumidityMidnight, String visibility, String visibilityNight, String windDirectionDay, String windDirectionNight, String windSpeedMPH, String windSpeedMPHNight, String maxUVIndex, String weatherType, String weatherTypeNight, String precipitationProbabilityDay, String getPrecipitationProbabilityNight) {
        this.forecastDate = forecastDate;
        this.forecastStartHour = forecastStartHour;
        this.forecastDuration = forecastDuration;
        this.feelsLikeDayTemperature = feelsLikeDayTemperature;
        this.feelsLikeNightTemperature = feelsLikeNightTemperature;
        this.dayMaximumTemperature = dayMaximumTemperature;
        this.nightMaximumTemperature = nightMaximumTemperature;
        this.windGustNoon = windGustNoon;
        this.windGustMidnight = windGustMidnight;
        this.screenRelativeHumidityNoon = screenRelativeHumidityNoon;
        this.getScreenRelativeHumidityMidnight = getScreenRelativeHumidityMidnight;
        this.visibility = visibility;
        this.visibilityNight = visibilityNight;
        this.windDirectionDay = windDirectionDay;
        this.windDirectionNight = windDirectionNight;
        this.windSpeedMPH = windSpeedMPH;
        this.windSpeedMPHNight = windSpeedMPHNight;
        this.maxUVIndex = maxUVIndex;
        this.weatherType = weatherType;
        this.weatherTypeNight = weatherTypeNight;
        this.precipitationProbabilityDay = precipitationProbabilityDay;
        this.getPrecipitationProbabilityNight = getPrecipitationProbabilityNight;
    }

    public String getForecastDate() {
        return forecastDate;
    }

    public String getForecastStartHour() {
        return forecastStartHour;
    }

    public String getForecastDuration() {
        return forecastDuration;
    }

    public String getFeelsLikeDayTemperature() {
        return feelsLikeDayTemperature;
    }

    public String getFeelsLikeNightTemperature() {
        return feelsLikeNightTemperature;
    }

    public String getVisibilityNight() {
        return visibilityNight;
    }

    public String getWindDirectionNight() {
        return windDirectionNight;
    }

    public String getWindSpeedMPHNight() {
        return windSpeedMPHNight;
    }

    public String getWeatherTypeNight() {
        return weatherTypeNight;
    }

    public String getDayMaximumTemperature() {
        return dayMaximumTemperature;
    }

    public String getNightMaximumTemperature() {
        return nightMaximumTemperature;
    }

    public String getWindGustNoon() {
        return windGustNoon;
    }

    public String getWindGustMidnight() {
        return windGustMidnight;
    }

    public String getScreenRelativeHumidityNoon() {
        return screenRelativeHumidityNoon;
    }

    public String getGetScreenRelativeHumidityMidnight() {
        return getScreenRelativeHumidityMidnight;
    }

    public String getVisibility() {
        return visibility;
    }

    public String getWindDirectionDay() {
        return windDirectionDay;
    }

    public String getWindSpeedMPH() {
        return windSpeedMPH;
    }

    public String getMaxUVIndex() {
        return maxUVIndex;
    }

    public String getWeatherType() {
        return weatherType;
    }

    public String getPrecipitationProbabilityDay() {
        return precipitationProbabilityDay;
    }

    public String getGetPrecipitationProbabilityNight() {
        return getPrecipitationProbabilityNight;
    }
}
