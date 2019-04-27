package com.example.android.distributeurdeau.models;


import java.sql.Date;
import java.util.Random;

import jade.util.leap.Serializable;

public class Plot implements Serializable {
    private static final long serialVersionUID = 3L;

    private Farmer farmer;
    private String p_name;
    private float area;
    private float water_qte;
    private Date s_date;
    private String type;
    private int status;
    public float Kc;
    public float ET0;
    public float PLUIE;
    public float Ym;
    public float Ky;
    private float dotation;

    public float getDotation() {
        return dotation;
    }

    public void setDotation(float dotation) {
        this.dotation = dotation;
    }

    public Plot(Farmer farmer, String p_name, String type, float area, float water_qte, Date s_date) {

        this.p_name = p_name;
        this.farmer = farmer;
        this.type = type;
        this.area = area;
        this.s_date = s_date;
        this.water_qte = water_qte;
        this.status = 0;
        Random rand = new Random(1);
        Kc = rand.nextFloat()%0.1f;
        rand.setSeed(4);
        ET0 = (rand.nextFloat()+2)%4;
        PLUIE = 0;
        Ym = 2;
        Ky = 1.05f;
    }

    public Plot(Plot p) {
        this.p_name = p.p_name;
        this.farmer = p.farmer;
        this.type = p.type;
        this.area = p.area;
        this.s_date = p.s_date;
        this.water_qte = p.water_qte;
        this.status = p.status;
        this.dotation = p.dotation;
        this.Kc = p.Kc;
        this.ET0 = p.ET0;
        this.PLUIE = p.PLUIE;
        this.Ym = p.Ym;
        this.Ky = p.Ky;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Farmer getFarmer() {
        return farmer;
    }

    public void setFarmer(Farmer farmer) {
        this.farmer = farmer;
    }

    public String getP_name() {
        return p_name;
    }

    public void setP_name(String p_name) {
        this.p_name = p_name;
    }

    public float getArea() {
        return area;
    }

    public void setArea(float area) {
        this.area = area;
    }

    public float getWater_qte() {
        return water_qte;
    }

    public void setWater_qte(float water_qte) {
        this.water_qte = water_qte;
    }

    public Date getS_date() {
        return s_date;
    }

    public void setS_date(Date s_date) {
        this.s_date = s_date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getKc() {
        return Kc;
    }

    public void setKc(float kc) {
        Kc = kc;
    }

    public float getET0() {
        return ET0;
    }

    public void setET0(float ET0) {
        this.ET0 = ET0;
    }

    public float getPLUIE() {
        return PLUIE;
    }

    public void setPLUIE(float PLUIE) {
        this.PLUIE = PLUIE;
    }

    public float getYm() {
        return Ym;
    }

    public void setYm(float ym) {
        Ym = ym;
    }

    public float getKy() {
        return Ky;
    }

    public void setKy(float ky) {
        Ky = ky;
    }

    @Override
    public String toString() {
        return "Plot{" +
                "farmer_num=" + farmer.getFarmer_num() +
                ", p_name='" + p_name + '\'' +
                ", area=" + area +
                ", water_qte=" + water_qte +
                ", s_date=" + s_date +
                ", type='" + type + '\'' +
                ", status=" + status +
                ", Kc=" + Kc +
                ", ET0=" + ET0 +
                ", PLUIE=" + PLUIE +
                ", Ym=" + Ym +
                ", Ky=" + Ky +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Plot)) return false;
        Plot plot = (Plot) o;
        return Float.compare(plot.area, area) == 0 &&
                Float.compare(plot.water_qte, water_qte) == 0 &&
                s_date.equals(plot.s_date) &&
                type.equals(plot.type);
    }

}
