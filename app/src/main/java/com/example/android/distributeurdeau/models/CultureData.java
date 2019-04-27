package com.example.android.distributeurdeau.models;

import java.sql.Date;

import jade.util.leap.Serializable;

public class CultureData implements Serializable {
    private String name;
    private Date start_s_date;
    private Date end_s_date;
    private float price;
    private float v_price;
    private float f_price;

    public CultureData(String name, Date start_s_date, Date end_s_date, float price, float v_price, float f_price) {
        this.name = name;
        this.start_s_date = start_s_date;
        this.end_s_date = end_s_date;
        this.price = price;
        this.v_price = v_price;
        this.f_price = f_price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStart_s_date() {
        return start_s_date;
    }

    public void setStart_s_date(Date start_s_date) {
        this.start_s_date = start_s_date;
    }

    public Date getEnd_s_date() {
        return end_s_date;
    }

    public void setEnd_s_date(Date end_s_date) {
        this.end_s_date = end_s_date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getV_price() {
        return v_price;
    }

    public void setV_price(float v_price) {
        this.v_price = v_price;
    }

    public float getF_price() {
        return f_price;
    }

    public void setF_price(float f_price) {
        this.f_price = f_price;
    }

    @Override
    public String toString() {
        return "Culture_data{" +
                "name='" + name + '\'' +
                ", start_s_date=" + start_s_date +
                ", end_s_date=" + end_s_date +
                ", price=" + price +
                ", v_price=" + v_price +
                ", f_price=" + f_price +
                '}';
    }
}
