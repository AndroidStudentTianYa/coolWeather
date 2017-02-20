package com.hfw.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by FuWei on 2017/2/20.
 */
public class City extends DataSupport{
    private String cityName;
    private int id;
    private int cityCode;
    private int provinceId;

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getprovinceId() {
        return provinceId;
    }

    public void setprovinceId(int provinceId) {
        this.provinceId = provinceId;
    }
}
