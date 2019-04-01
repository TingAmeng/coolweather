package com.example.coolweather.db;

import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {
    //LitePal 中的每一个实体类都必须继承自 LitePalSupport

    private int id;     // id是每个实体类都应该有的字段
    private String provinceName;  //省名
    private int proviceCode;     //省代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProviceCode() {
        return proviceCode;
    }

    public void setProviceCode(int proviceCode) {
        this.proviceCode = proviceCode;
    }
}
