package com.example.android.distributeurdeau.farmer;

import com.example.android.distributeurdeau.models.CultureData;
import com.example.android.distributeurdeau.models.Plot;

import java.util.Vector;

public interface FarmerInterface {
    void modifyPlot(Plot plot);

    void addPlot(Plot plot);

    void sendPlot(String plotName, String farmerNum, float waterQte);

    void deletePlot(String plotName, String farmerNum);

    void cancelNegotiation(String plotName, String farmerNum);

    Vector<CultureData> getCultureData();
}
