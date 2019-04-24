package com.example.android.distributeurdeau.models;

import java.util.Vector;

import jade.util.leap.Serializable;

public class Supervisor implements Serializable{
    private static final long serialVersionUID = 5L;

    private String f_name;
    private String l_name;
    private String id;
    private String password;
    private Vector<Farmer> farmers;

    public Supervisor(String f_name, String l_name, String id, String password) {
        this.f_name = f_name;
        this.l_name = l_name;
        this.id = id;
        this.password = password;
        this.farmers = new Vector<>();
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

    public String getId() {
        return id;
    }

    public void setId(String farmer_num) {
        this.id = farmer_num;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Vector<Farmer> getFarmers() {
        return farmers;
    }

    public void setFarmers(Vector<Farmer> farmers) {
        this.farmers = farmers;
    }
}
