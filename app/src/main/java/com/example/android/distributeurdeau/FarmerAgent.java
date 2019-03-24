package com.example.android.distributeurdeau;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.distributeurdeau.models.Database;
import com.example.android.distributeurdeau.models.Farmer;
import com.example.android.distributeurdeau.models.Plot;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.MicroRuntime;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class FarmerAgent extends Agent implements FarmerInterface{
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

        registerO2AInterface(FarmerInterface.class, this);

        final MessageTemplate template = MessageTemplate.and(
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                        MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
                ),
                MessageTemplate.MatchOntology("plot-modification")
        );
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive(template);
                if (message != null) {
                    if (message.getPerformative() == ACLMessage.CONFIRM) {
                        Intent broadcast = new Intent();
                        broadcast.setAction("success");
                        context.sendBroadcast(broadcast);
                    } else if (message.getPerformative() == ACLMessage.FAILURE) {
                        Intent broadcast = new Intent();
                        broadcast.setAction("success");
                        context.sendBroadcast(broadcast);
                    }
                } else {
                    block();
                }
            }
        });
    }

    @Override
    public void modifyPlot(Plot plot) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.setOntology("plot-modification");
        try {
            message.setContentObject(plot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send(message);
    }
}
