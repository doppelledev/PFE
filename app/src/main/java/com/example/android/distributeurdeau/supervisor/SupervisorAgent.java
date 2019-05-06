package com.example.android.distributeurdeau.supervisor;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.distributeurdeau.constants.Database;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.constants.Templates;
import com.example.android.distributeurdeau.models.CultureData;
import com.example.android.distributeurdeau.models.Plot;
import com.example.android.distributeurdeau.models.Supervisor;

import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class SupervisorAgent extends Agent implements SupervisorInterface {

    private static final String TAG = "SupervisorAgent";

    private Context context;
    private Supervisor supervisor;
    private Vector<CultureData> cultureData;

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


        addBehaviour(new NotificationBehaviour());
        addBehaviour(new DotationBehaviour());
        // Get culture data
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology(Strings.ONTOLOGY_CULTURE_DATA);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        send(message);
    }

    public Vector<CultureData> getCultureData() {
        return cultureData;
    }


    @Override
    public void setDotation(String pname, String fnum, float dotation) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.setOntology(Strings.ONTOLOGY_DOTATION);
        message.addUserDefinedParameter(Database.p_name, pname);
        message.addUserDefinedParameter(Database.farmer_num, fnum);
        message.addUserDefinedParameter(Database.dotation, String.valueOf(dotation));
        send(message);
    }


    private class DotationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.DOTATION);
            if (message != null) {
                if (message.getPerformative() == ACLMessage.CONFIRM) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_DOTATION_SUCCESS);
                    context.sendBroadcast(broadcast);
                } else if (message.getPerformative() == ACLMessage.FAILURE) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_DOTATION_FAILED);
                    context.sendBroadcast(broadcast);
                }
            } else {
                block();
            }
        }
    }

    private class NotificationBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.NOTIFICATION);
            if (message != null) {
                if (message.getPerformative() == ACLMessage.INFORM) {
                    try {
                       int state = Integer.valueOf(message.getUserDefinedParameter(Strings.ONTOLOGY_NOTIFY));
                        Log.d(TAG, "action: is send:" + state);
                        Plot plot = (Plot) message.getContentObject();
                        Intent broadcast = new Intent();
                        broadcast.setAction(Strings.ACTION_NOTIFY);
                        broadcast.putExtra(Strings.EXTRA_INT, state);
                        broadcast.putExtra(Strings.EXTRA_PLOT, plot);
                        context.sendBroadcast(broadcast);
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                block();
            }
        }
    }

}
