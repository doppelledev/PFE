package com.example.android.distributeurdeau.models;

import jade.util.leap.Serializable;

import java.util.Vector;

public class Farmer implements Serializable {
    private static final long serialVersionUID = 4L;

    private String f_name;
    private String l_name;
    private String farmer_num;
    private String password;
    private boolean risk;
    private Vector<Plot> plots = new Vector<>();

    public Farmer(String farmer_num, String f_name, String l_name, String password, boolean risk) {
        this.f_name = f_name;
        this.l_name = l_name;
        this.farmer_num = farmer_num;
        this.password = password;
        this.risk = risk;
    }

    public String getF_name() {
        return f_name;
    }

    public void setF_name(String f_name) {
        this.f_name = f_name;
    }

    public String getL_name() {
        return l_name;
    }

    public void setL_name(String l_name) {
        this.l_name = l_name;
    }

    public String getFarmer_num() {
        return farmer_num;
    }

    public void setFarmer_num(String farmer_num) {
        this.farmer_num = farmer_num;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRisk() {
        return risk;
    }

    public void setRisk(boolean risk) {
        this.risk = risk;
    }

    public Vector<Plot> getPlots() {
        return plots;
    }

    public void setPlots(Vector<Plot> plots) {
        this.plots = plots;
    }

    @Override
    public String toString() {
        return "Farmer{" +
                "f_name='" + f_name + '\'' +
                ", l_name='" + l_name + '\'' +
                ", farmer_num='" + farmer_num + '\'' +
                ", password='" + password + '\'' +
                ", risk=" + risk +
                '}';
    }
}
