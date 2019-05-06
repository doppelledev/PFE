package com.example.android.distributeurdeau.supervisor;

import com.example.android.distributeurdeau.models.CultureData;

import java.util.Vector;

public interface SupervisorInterface {
    Vector<CultureData> getCultureData();
    void setDotation(String pname, String fnum, float dotation);
}
