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

import java.io.IOException;
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

        addBehaviour(new CultureDataBehaviour());
        addBehaviour(new ProposalStatusBehaviour());

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
    public void propose(Plot proposedPlot) {
        ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.setOntology(Strings.ONTOLOGY_PROPOSE);
        try {
            message.setContentObject(proposedPlot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send(message);
    }

    @Override
    public void endNegotiation(String plotName, String farmerNum) {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.setOntology(Strings.ONTOLOGY_END);
        message.addUserDefinedParameter(Database.p_name, plotName);
        message.addUserDefinedParameter(Database.farmer_num, farmerNum);
        send(message);
    }

    private class CultureDataBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.CULTURE_DATA);
            if (message != null) {
                if (message.getPerformative() == ACLMessage.CONFIRM) {
                    try {
                        cultureData = (Vector<CultureData>) message.getContentObject();
                        Log.d(TAG, "action: receviec data: " + cultureData.size());
                    } catch (UnreadableException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                block();
            }
        }
    }

    private class ProposalStatusBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.PROPOSAL_STATUS);
            if (message != null) {
                if (message.getPerformative() == ACLMessage.CONFIRM) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_PROPOSAL_SENT);
                    context.sendBroadcast(broadcast);
                } else if (message.getPerformative() == ACLMessage.FAILURE) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_PROPOSAL_FAILED);
                    context.sendBroadcast(broadcast);
                }
            } else {
                block();
            }
        }
    }

}
