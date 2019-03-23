package com.example.android.distributeurdeau;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.distributeurdeau.models.Database;
import com.example.android.distributeurdeau.models.Farmer;

import jade.android.MicroRuntimeService;
import jade.core.AID;
import jade.core.Agent;
import jade.core.MicroRuntime;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class FarmerAgent extends Agent implements LoginInterface{
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

        Log.d(TAG, "setup: *icro running? " + MicroRuntime.isRunning());
        Log.d(TAG, "setup: sending broadcats");
        Intent broadcast = new Intent();
        broadcast.setAction(LoginActivity.START_FARMER_ACTIVITY);
        broadcast.putExtra("farmer", farmer);
        context.sendBroadcast(broadcast);

        registerO2AInterface(LoginInterface.class, this);
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage m = receive();
                if (m != null) {
                    Log.d(TAG, "action: received" + m.getContent());
                } else {
                    block();
                }
            }
        });
    }

    @Override
    public void authenticate(String numAgr, String pass) {
        Log.d(TAG, "authenticate: sending msg");
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology("authentication");
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.addUserDefinedParameter(Database.farmer_num, numAgr);
        message.addUserDefinedParameter(Database.password, pass);
        send(message);
    }

    @Override
    public void register(Farmer farmer) {

    }
}
