package com.example.android.distributeurdeau;

import com.example.android.distributeurdeau.models.CultureData;
import com.example.android.distributeurdeau.models.Plot;

import java.util.Vector;

public class Estimation {

    private Vector<CultureData> cultureData;

    public Estimation(Vector<CultureData> cultureData) {
        this.cultureData = cultureData;
    }

    public float estimateBesoin(Plot plot) {
        return (plot.Kc * plot.ET0 - plot.PLUIE) * plot.getArea();
    }

    public float estimateRendement(Plot plot) {
        //besoin
        float etcAdj = plot.getWater_qte()*0.007f + plot.PLUIE * plot.getArea();
        float etc = plot.Kc * plot.ET0 * plot.getArea();
        return (plot.Ky * (1 - etcAdj / etc) - 1) * plot.Ym * -1;
    }

    public float estimateProfit(Plot plot) {
        return estimateRendement(plot) * getPriceFromCultureData(plot.getType()) * plot.getArea();
    }

    public float getPriceFromCultureData(String type) {
        for (CultureData data : cultureData) {
            if (data.getName().equals(type))
                return data.getPrice();
        }
        return 300.0f;
    }
}