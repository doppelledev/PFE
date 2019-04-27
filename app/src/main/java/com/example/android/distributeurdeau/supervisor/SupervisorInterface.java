package com.example.android.distributeurdeau.supervisor;

import com.example.android.distributeurdeau.models.CultureData;
import com.example.android.distributeurdeau.models.Plot;

import java.util.Vector;

public interface SupervisorInterface {
    Vector<CultureData> getCultureData();
    void propose(Plot proposedPlot);
    void endNegotiation(String plotName, String farmerNum);
}
