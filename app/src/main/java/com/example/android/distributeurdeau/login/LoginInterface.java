package com.example.android.distributeurdeau.login;

import com.example.android.distributeurdeau.models.Farmer;

public interface LoginInterface {
    void authenticate(String numAgr, String pass, boolean isFarmer);

    void register(Farmer farmer);
}
