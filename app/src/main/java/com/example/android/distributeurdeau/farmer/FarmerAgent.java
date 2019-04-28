package com.example.android.distributeurdeau.farmer;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.android.distributeurdeau.constants.Database;
import com.example.android.distributeurdeau.constants.Strings;
import com.example.android.distributeurdeau.constants.Templates;
import com.example.android.distributeurdeau.models.CultureData;
import com.example.android.distributeurdeau.models.Farmer;
import com.example.android.distributeurdeau.models.Plot;

import java.io.IOException;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class FarmerAgent extends Agent implements FarmerInterface {
    private static final String TAG = "FarmerAgent";

    private Context context;
    private Farmer farmer;
    private Vector<CultureData> cultureData;

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length >= 2) {
            context = (Context) getArguments()[0];
            farmer = (Farmer) getArguments()[1];
            Log.d(TAG, "setup: farmer agent: " + farmer);
        } else {
            Log.d(TAG, "setup: wrong number of arguments");
        }

        Intent broadcast = new Intent();
        broadcast.setAction(Strings.ACTION_LAUNCH_FARMER);
        broadcast.putExtra(Strings.EXTRA_FARMER, farmer);
        context.sendBroadcast(broadcast);

        // Activate GUI
        registerO2AInterface(FarmerInterface.class, this);

        // Ad behaviour
        addBehaviour(new ModificationBehaviour());
        addBehaviour(new AdditionBehaviour());
        addBehaviour(new SendBehaviour());
        addBehaviour(new DeleteBehaviour());
        addBehaviour(new CultureDataBehaviour());
        addBehaviour(new CancelBehaviour());
        // Get culture data
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology(Strings.ONTOLOGY_CULTURE_DATA);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        send(message);
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

    @Override
    public void addPlot(Plot plot) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.setOntology(Strings.ONTOLOGY_ADD);
        try {
            message.setContentObject(plot);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send(message);
    }

    @Override
    public void sendPlot(String plotName, String farmerNum, float waterQte) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.setOntology(Strings.ONTOLOGY_SEND);
        message.addUserDefinedParameter(Database.p_name, plotName);
        message.addUserDefinedParameter(Database.farmer_num, farmerNum);
        message.addUserDefinedParameter(Database.water_qte, String.valueOf(waterQte));
        send(message);
    }

    @Override
    public void deletePlot(String plotName, String farmerNum) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.setOntology(Strings.ONTOLOGY_DELETE);
        message.addUserDefinedParameter(Database.p_name, plotName);
        message.addUserDefinedParameter(Database.farmer_num, farmerNum);
        send(message);
    }

    @Override
    public void cancelNegotiation(String plotName, String farmerNum) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.setOntology(Strings.ONTOLOGY_CANCEL);
        message.addUserDefinedParameter(Database.p_name, plotName);
        message.addUserDefinedParameter(Database.farmer_num, farmerNum);
        send(message);
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

    private class AdditionBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.ADDITION);
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

    private class SendBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.SEND);
            if (message != null) {
                if (message.getPerformative() == ACLMessage.CONFIRM) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_SEND_SUCCEEDED);
                    context.sendBroadcast(broadcast);
                } else if (message.getPerformative() == ACLMessage.FAILURE) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_SEND_FAILED);
                    context.sendBroadcast(broadcast);
                }
            } else {
                block();
            }
        }
    }

    private class DeleteBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.DELETE);
            if (message != null) {
                if (message.getPerformative() == ACLMessage.CONFIRM) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_DELETE_SUCCEEDED);
                    context.sendBroadcast(broadcast);
                } else if (message.getPerformative() == ACLMessage.FAILURE) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_DELETE_FAILED);
                    context.sendBroadcast(broadcast);
                }
            } else {
                block();
            }
        }
    }

    private class CultureDataBehaviour extends CyclicBehaviour{
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

    private class CancelBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage message = receive(Templates.CANCEL_NEGOTIATION);
            if (message != null) {
                if (message.getPerformative() == ACLMessage.CONFIRM) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_CANCEL_SUCCEEDED);
                    context.sendBroadcast(broadcast);
                } else if (message.getPerformative() == ACLMessage.FAILURE) {
                    Intent broadcast = new Intent();
                    broadcast.setAction(Strings.ACTION_CANCEL_FAILED);
                    context.sendBroadcast(broadcast);
                }
            } else {
                block();
            }
        }
    }

    public Vector<CultureData> getCultureData() {
        return cultureData;
    }
}
