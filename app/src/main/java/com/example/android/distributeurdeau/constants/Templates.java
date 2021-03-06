package com.example.android.distributeurdeau.constants;

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

    public static final MessageTemplate CULTURE_DATA = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE),
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_CULTURE_DATA)
    );

    public static final MessageTemplate PROPOSAL_STATUS = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_PROPOSE)
    );

    public static final MessageTemplate CANCEL_NEGOTIATION = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_CANCEL)
    );

    public static final MessageTemplate NOTIFICATION = MessageTemplate.and(
            MessageTemplate.MatchPerformative(ACLMessage.INFORM),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_NOTIFY)
    );

    public static final MessageTemplate ACCEPT = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_ACCEPT)
    );

    public static final MessageTemplate REFUSE = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_REFUSE)
    );

    public static final MessageTemplate DOTATION = MessageTemplate.and(
            MessageTemplate.or(
                    MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                    MessageTemplate.MatchPerformative(ACLMessage.FAILURE)
            ),
            MessageTemplate.MatchOntology(Strings.ONTOLOGY_DOTATION)
    );
}
