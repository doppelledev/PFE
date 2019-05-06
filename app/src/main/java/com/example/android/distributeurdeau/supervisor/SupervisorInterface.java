package com.example.android.distributeurdeau.supervisor;

import com.example.android.distributeurdeau.models.CultureData;
import com.example.android.distributeurdeau.models.Plot;

import java.util.Vector;

public interface SupervisorInterface {
    Vector<CultureData> getCultureData();
    void propose(Plot proposedPlot);
    void accept(String plotName, String farmerNum);
    void setDotation(String pname, String fnum, float dotation);
}
