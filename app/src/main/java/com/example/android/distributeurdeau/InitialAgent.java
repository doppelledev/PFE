package com.example.android.distributeurdeau;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.example.android.distributeurdeau.models.Database;
import com.example.android.distributeurdeau.models.Farmer;

import java.io.IOException;

/**
 * This agent implements the logic of the chat client running on the user
 * terminal. User interactions are handled by the ChatGui in a
 * terminal-dependent way. The ChatClientAgent performs 3 types of behaviours: -
 * ParticipantsManager. A CyclicBehaviour that keeps the list of participants up
 * to date on the basis of the information received from the ChatManagerAgent.
 * This behaviour is also in charge of subscribing as a participant to the
 * ChatManagerAgent. - ChatListener. A CyclicBehaviour that handles messages
 * from other chat participants. - ChatSpeaker. A OneShotBehaviour that sends a
 * message conveying a sentence written by the user to other chat participants.
 *
 * @author Giovanni Caire - TILAB
 */
public class InitialAgent extends Agent implements LoginInterface {
    private static final String TAG = "InitaltAgent";
    private static final long serialVersionUID = 1594371294421614291L;

    private static final String CHAT_ID = "__chat__";

    private Context context;

    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Context) {
                context = (Context) args[0];
            }
        }

        // Register language and ontology


        // Add initial behaviours
        addBehaviour(new ChatListener(this));

        // Activate the GUI
        registerO2AInterface(LoginInterface.class, this);
        Log.d(TAG, "setup: O2A: " + getO2AInterface(LoginInterface.class));
        Intent broadcast = new Intent();
        broadcast.setAction(Strings.ACTION_LAUNCH_LOGIN);
        context.sendBroadcast(broadcast);

        final MessageTemplate template = MessageTemplate.and(
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                        MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
                ),
                MessageTemplate.MatchOntology("authentication")
        );

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive(template);
                if (message != null) {
                    Log.d(TAG, "action: message received");
                    if (message.getPerformative() == ACLMessage.CONFIRM) {
                        Farmer f = null;
                        try {
                            f = (Farmer) message.getContentObject();
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "action: farmer" + f);
                        Intent intent = new Intent();
                        intent.setAction(Strings.ACTION_LOGIN_SUCCEEDED);
                        intent.putExtra("farmer", f);
                        context.sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent();
                        intent.setAction(Strings.ACTION_LOGIN_FAILED);
                        context.sendBroadcast(intent);
                    }
                } else {
                    block();
                }
            }
        });

        final MessageTemplate regTemplate = MessageTemplate.and(
                MessageTemplate.or(
                        MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                        MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
                ),
                MessageTemplate.MatchOntology("registration")
        );
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage message = receive(regTemplate);
                if (message != null) {
                    String action;
                    if (message.getPerformative() == ACLMessage.CONFIRM)
                        action = RegisterActivity.CREATED;
                    else
                        action = RegisterActivity.FAILED;

                    Intent broadcast = new Intent();
                    broadcast.setAction(action);
                    context.sendBroadcast(broadcast);
                    Log.d(TAG, "action: " + action);
                } else {
                    block();
                }
            }
        });
    }

    protected void takeDown() {
    }

    @Override
    public void authenticate(String numAgr, String pass) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology("authentication");
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        message.addUserDefinedParameter(Database.farmer_num, numAgr);
        message.addUserDefinedParameter(Database.password, pass);
        send(message);
    }

    @Override
    public void register(Farmer farmer) {
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setOntology("registration");
        message.addReceiver(new AID(Database.manager, AID.ISLOCALNAME));
        try {
            message.setContentObject(farmer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        send(message);
    }

    /**
     * Inner class ChatListener. This behaviour registers as a chat participant
     * and keeps the list of participants up to date by managing the information
     * received from the ChatManager agent.
     */
    class ChatListener extends CyclicBehaviour {
        private static final long serialVersionUID = 741233963737842521L;
        private MessageTemplate template = MessageTemplate
                .MatchConversationId(CHAT_ID);

        ChatListener(Agent a) {
            super(a);
        }

        public void action() {
            ACLMessage msg = myAgent.receive(template);
            if (msg != null) {
                if (msg.getPerformative() == ACLMessage.INFORM) {
                    Log.d(TAG, "action: message received: " + msg.getContent());
                } else {
                    Log.d(TAG, "action: something went wrong");
                }
            } else {
                block();
            }
        }
    } // END of inner class ChatListener
}
