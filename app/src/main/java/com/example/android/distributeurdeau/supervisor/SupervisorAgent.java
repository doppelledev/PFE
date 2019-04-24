package com.example.android.distributeurdeau.supervisor;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.distributeurdeau.Strings;
import com.example.android.distributeurdeau.models.Supervisor;

import jade.core.Agent;

public class SupervisorAgent extends Agent implements SupervisorInterface {

    private static final String TAG = "SupervisorAgent";

    private Context context;
    private Supervisor supervisor;

    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Context) {
                context = (Context) args[0];
                supervisor = (Supervisor) getArguments()[1];
                Log.d(TAG, "setup: " + supervisor.getF_name());
            } else {
                Log.d(TAG, "setup: wrong number of arguments");
            }
        }

        // send a broadcast to start LoginActivity
        Intent broadcast = new Intent();
        broadcast.setAction(Strings.ACTION_LAUNCH_SUPERVISOR);
        broadcast.putExtra(Strings.EXTRA_SUPERVISOR, supervisor);
        context.sendBroadcast(broadcast);

        // activate the gui
        registerO2AInterface(SupervisorInterface.class, this);
        Log.d(TAG, "setup: O2A: " + getO2AInterface(SupervisorInterface.class));

    }


}
