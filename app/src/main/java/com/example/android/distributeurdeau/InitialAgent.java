package com.example.android.distributeurdeau;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.distributeurdeau.models.Database;
import com.example.android.distributeurdeau.models.Farmer;

import java.io.IOException;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class InitialAgent extends Agent implements LoginInterface {
    private static final String TAG = "InitialAgent";

    private Context context;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Context) {
                context = (Context) args[0];
            }
        }

        // Activate the GUI
        registerO2AInterface(LoginInterface.class, this);
        Log.d(TAG, "setup: O2A: " + getO2AInterface(LoginInterface.class));

        // send a broadcast to start LoginActivity
        Intent broadcast = new Intent();
        broadcast.setAction(Strings.ACTION_LAUNCH_LOGIN);
        context.sendBroadcast(broadcast);

        // Add authentication behaviour
        addBehaviour(new AuthBehaviour());

        // Add registration behaviour
        addBehaviour(new RegBehaviour());
    }

    @Override
    public void authenticate(String numAgr, String pass) {
        // Send an authentication request to the server with the provided credentials
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology(Strings.ONTOLOGY_AUTH);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.addUserDefinedParameter(Database.farmer_num, numAgr);
        message.addUserDefinedParameter(Database.password, pass);
        send(message);
    }

    @Override
    public void register(Farmer farmer) {
        // Send a registration request to the server with the provided data
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology(Strings.ONTOLOGY_REG);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        try {
            message.setContentObject(farmer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send(message);
    }

    private class AuthBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.AUTHENTICATION);
            if (message != null) {
                Log.d(TAG, "action: auth message received");
                if (message.getPerformative() == ACLMessage.CONFIRM) {
                    // The server authenticated the user
                    // Retrieve his information
                    Farmer f = null;
                    try {
                        f = (Farmer) message.getContentObject();
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "action: farmer" + f);
                    // And send a broadcast to launch PlotActivity
                    Intent intent = new Intent();
                    intent.setAction(Strings.ACTION_LOGIN_SUCCEEDED);
                    intent.putExtra(Strings.EXTRA_FARMER, f);
                    context.sendBroadcast(intent);
                } else {
                    // The server couldn't authenticate the user
                    // Send a broadcast to inform him
                    Intent intent = new Intent();
                    intent.setAction(Strings.ACTION_LOGIN_FAILED);
                    context.sendBroadcast(intent);
                }
            } else {
                block();
            }
        }
    }

    private class RegBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.REGISTRATION);
            if (message != null) {
                String action;
                if (message.getPerformative() == ACLMessage.CONFIRM)
                    action = Strings.ACTION_REGISTRATION_SUCCEEDED;
                else
                    action = Strings.ACTION_REGISTRATION_FAILED;

                // Inform the RegisterActivity whether the registration succeeded or not
                Intent broadcast = new Intent();
                broadcast.setAction(action);
                context.sendBroadcast(broadcast);
                Log.d(TAG, "action: " + action);
            } else {
                block();
            }
        }
    }
}
