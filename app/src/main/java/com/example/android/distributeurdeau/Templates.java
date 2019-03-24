package com.example.android.distributeurdeau;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Templates {

    public static final MessageTemplate AUTHENTICATION = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
            ),
            MessageTemplate.MatchOntology("authentication")
    );

    public static final MessageTemplate REGISTRATION = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
            ),
            MessageTemplate.MatchOntology("registration")
    );
}
