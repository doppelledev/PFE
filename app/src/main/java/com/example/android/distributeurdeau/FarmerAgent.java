package com.example.android.distributeurdeau;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.distributeurdeau.models.Farmer;

import jade.core.Agent;

public class FarmerAgent extends Agent {
    private static final String TAG = "FarmerAgent";

    private Context context;
    private Farmer farmer;

    @Override
    protected void setup() {
        Object [] args = getArguments();
        if (args != null && args.length >= 2) {
            context = (Context) getArguments()[0];
            farmer = (Farmer) getArguments()[1];
            Log.d(TAG, "setup: farmer agent wohoo: " + farmer);
        } else {
            Log.d(TAG, "setup: wrong number of arguments");
        }

        Log.d(TAG, "setup: sending broadcats");
        Intent broadcast = new Intent();
        broadcast.setAction(LoginActivity.START_FARMER_ACTIVITY);
        broadcast.putExtra("farmer", farmer);
        context.sendBroadcast(broadcast);
    }
}
