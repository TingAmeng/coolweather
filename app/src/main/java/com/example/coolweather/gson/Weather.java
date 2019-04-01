package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Weather为总的实体类，来引用JSON包中的其他实体类
 * */

public class Weather {

    public String status;
    public Basic basic;
    public AQI aqi;
    public Now now;
    public Suggestion suggestion;

    //由于daily_forecast 中包含的是一个数组，使用 List集合来引用 Forecast类
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
