package com.aiplayground.mcpjira.service;

import com.aiplayground.mcpjira.client.JiraHttpClient;
import com.aiplayground.mcpjira.model.JiraTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service class for interacting with Jira tickets.
 * Provides methods to retrieve ticket information from Jira REST API.
 */
@Service
public class JiraTicketService {

    private final JiraHttpClient jiraHttpClient;

    @Autowired
    public JiraTicketService(JiraHttpClient jiraHttpClient) {
        this.jiraHttpClient = jiraHttpClient;
    }

    /**
     * Retrieves a Jira ticket by its key (e.g., "AI-6").
     * 
     * @param ticketKey The ticket key (e.g., "AI-6")
     * @return Mono containing the JiraTicket, or an error if the ticket is not found
     */
    public Mono<JiraTicket> getTicket(String ticketKey) {
        if (ticketKey == null || ticketKey.trim().isEmpty()) {
            return Mono.error(new IllegalArgumentException("Ticket key cannot be null or empty"));
        }

        String apiPath = "/rest/api/3/issue/" + ticketKey.trim();
        return jiraHttpClient.get(apiPath, JiraTicket.class);
    }
}

