package com.atguigu.gmall.bean;

import java.io.Serializable;

/**
 * 库存表
 */
public class WmsWareInfo implements Serializable {

    private String id;
    private String name;
    private String address;
    private String areacode; //地区编码

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAreacode() {
        return areacode;
    }

    public void setAreacode(String areacode) {
        this.areacode = areacode;
    }
}






















