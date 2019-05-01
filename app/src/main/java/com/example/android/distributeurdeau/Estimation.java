package com.example.android.distributeurdeau;

import com.example.android.distributeurdeau.models.CultureData;
import com.example.android.distributeurdeau.models.Plot;

import java.util.Vector;

public class Estimation {

    private Vector<CultureData> cultureData;

    public Estimation(Vector<CultureData> cultureData) {
        this.cultureData = cultureData;
    }

    public float estimateBesoin(Plot plot, float area) {
        return (plot.Kc * plot.ET0 - plot.PLUIE) * area;
    }

    public float estimateRendement(Plot plot, float area) {
        float etcAdj = (estimateBesoin(plot, area) + plot.PLUIE) * area;
        float etc = plot.Kc * plot.ET0 * area;
        return (plot.Ky * (1 - etcAdj / etc) - 1) * plot.Ym * -1;
    }

    public float estimateProfit(Plot plot, float area) {
        return estimateRendement(plot, area) * getPriceFromCultureData(plot.getType()) * area;
    }

    public float getPriceFromCultureData(String type) {
        for (CultureData data : cultureData) {
            if (data.getName().equals(type))
                return data.getPrice();
        }
        return 300.0f;
    }
}
