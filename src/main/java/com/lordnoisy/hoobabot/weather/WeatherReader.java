package com.lordnoisy.hoobabot.weather;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import org.json.*;

public class WeatherReader {
    private String key;
    public WeatherReader (String key) {
        this.key = key;
    }
    public ArrayList<Forecast> getDailyWeather() throws IOException {
        ArrayList<Forecast> weatherData = new ArrayList<>();
        URL url = new URL("http://datapoint.metoffice.gov.uk/public/data/val/wxfcs/all/json/353057?res=daily&key=" + key);  // example url which return json data
        JSONTokener tokener = new JSONTokener(url.openStream());

        JSONObject object = new JSONObject(tokener);

        object = object.getJSONObject("SiteRep");

        object = object.getJSONObject("DV");

        object = object.getJSONObject("Location");

        JSONArray array = object.getJSONArray("Period");

        /*
        Digest this data:
                        "type": "Day",
                        "value": "2022-01-09Z",
                        "Rep": [
                            {
                                "D": "WSW", "Gn": "18", "Hn": "75", "PPd": "7", "S": "7",
                                "V": "VG", "Dm": "9", "FDm": "8", "W": "7", "U": "1", "$": "Day"
                            },

                            FDm = Feels like day max temp
                            FNm = Feels Like Night Minimum Temperature
                            Dm = Max temp
                            Nm = night max temp
                            Gn = wind gust noon
                            Gm = wind gust midnight
                            Hn = screen relative humidity noon
                            V = visibility
                            D = wind direction
                            S = wind speed
                            U = max UV index
                            W = weather type
                            PPd = precipitation probability day
                            PPn = precipitation probability night
         */
        for (int i = 0; i < array.length(); i++) {
            JSONObject currentDayObject = array.getJSONObject(i);
            String date = currentDayObject.getString("value");

            //Get all the day data
            JSONObject dayForecastObject = currentDayObject.getJSONArray("Rep").getJSONObject(0);
            String windDirectionDay = dayForecastObject.getString("D");
            String windGustNoon = dayForecastObject.getString("Gn");
            String screenRelativeHumidityNoon = dayForecastObject.getString("Hn");
            String precipitationProbabilityDay = dayForecastObject.getString("PPd");
            String windSpeedDay = dayForecastObject.getString("S");
            String visibilityDay = dayForecastObject.getString("V");
            String maximumTemperatureDay = dayForecastObject.getString("Dm");
            String feelsLikeDayMaxTemperature = dayForecastObject.getString("FDm");
            String weatherType = dayForecastObject.getString("W");
            String maxUVIndex = dayForecastObject.getString("U");

            //Get all the night data
            JSONObject nightForecastObject = currentDayObject.getJSONArray("Rep").getJSONObject(1);
            String windDirectionNight = nightForecastObject.getString("D");
            String windGustMidnight = nightForecastObject.getString("Gm");
            String screenRelativeHumidityMidnight = nightForecastObject.getString("Hm");
            String precipitationProbabilityNight = nightForecastObject.getString("PPn");
            String windSpeedNight = nightForecastObject.getString("S");
            String visibilityNight = nightForecastObject.getString("V");
            String nightMaximumTemperature = nightForecastObject.getString("Nm");
            String feelsLikeNightMaximumTemperature = nightForecastObject.getString("FNm");
            String weatherTypeNight = nightForecastObject.getString("W");

            //Make a Forecast
            Forecast forecast = new Forecast(date, "00", "24", feelsLikeDayMaxTemperature,
                    feelsLikeNightMaximumTemperature, maximumTemperatureDay, nightMaximumTemperature, windGustNoon,
                    windGustMidnight, screenRelativeHumidityNoon, screenRelativeHumidityMidnight,
                    visibilityDay, visibilityNight, windDirectionDay, windDirectionNight, windSpeedDay, windSpeedNight, maxUVIndex,
                    weatherType, weatherTypeNight, precipitationProbabilityDay, precipitationProbabilityNight);

            weatherData.add(forecast);
        }

        return weatherData;
    }
}
