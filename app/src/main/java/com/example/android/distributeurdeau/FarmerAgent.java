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
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

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
            Log.d(TAG, "setup: farmer agent: " + farmer);
        } else {
            Log.d(TAG, "setup: wrong number of arguments");
        }

        Intent broadcast = new Intent();
        broadcast.setAction(Strings.ACTION_LAUNCH_FARMER);
        broadcast.putExtra("farmer", farmer);
        context.sendBroadcast(broadcast);

        // Activate GUI
        registerO2AInterface(FarmerInterface.class, this);

        // Ad behaviour
        addBehaviour(new ModificationBehaviour());
    }

    private class ModificationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.MODIFICATION);
            if (message != null) {
                if (message.getPerformative() == ACLMessage.CONFIRM) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_MODIFICATION_SUCCEEDED);
                    context.sendBroadcast(broadcast);
                } else if (message.getPerformative() == ACLMessage.FAILURE) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_MODIFICATION_FAILED);
                    context.sendBroadcast(broadcast);
                }
            } else {
                block();
            }
        }
    }

    @Override
    public void modifyPlot(Plot plot) {
        // send a request to the server to modify the plot
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.setOntology(Strings.ONTOLOGY_MDF);
        try {
            message.setContentObject(plot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send(message);
    }
}
