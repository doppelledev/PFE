package com.example.android.distributeurdeau;

import com.example.android.distributeurdeau.models.Farmer;

public interface LoginInterface {
    void authenticate(String numAgr, String pass);
    void register(Farmer farmer);
}
