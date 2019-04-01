package com.example.coolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    /**
     * 因为 JSON 中的一些字段不太适合直接作为 Java 字段命名
     * 所以使用@SerializedName()注解的方式让JSON字段和Java字段之间
     * 建立映射关系
     * */

    @SerializedName("city")
    public String cityName;   //城市名

    @SerializedName("id")
    public String weatherId;   //城市对应的天气id

    public Update update;

    public class Update{
        @SerializedName("loc")
        public String updateTime;   //天气更新时间
    }
}
