package com.example.android.distributeurdeau;

import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Templates {

    public static final MessageTemplate AUTHENTICATION = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.REFUSE)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_AUTH)
    );

    public static final MessageTemplate REGISTRATION = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_REG)
    );

    public static final MessageTemplate MODIFICATION = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_MDF)
    );

    public static final MessageTemplate ADDITION = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_ADD)
    );

    public static final MessageTemplate SEND = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_SEND)
    );

    public static final MessageTemplate DELETE = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_DELETE)
    );
}
