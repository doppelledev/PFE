package com.example.android.distributeurdeau.farmer;

import com.example.android.distributeurdeau.models.Plot;

public interface FarmerInterface {
    void modifyPlot(Plot plot);

    void addPlot(Plot plot);

    void sendPlot(String plotName, String farmerNum);

    void deletePlot(String plotName, String farmerNum);
}
