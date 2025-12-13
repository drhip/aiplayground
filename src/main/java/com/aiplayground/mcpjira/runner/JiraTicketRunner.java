package com.aiplayground.mcpjira.runner;

import com.aiplayground.mcpjira.model.JiraTicket;
import com.aiplayground.mcpjira.service.JiraTicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * CommandLineRunner to fetch and display Jira ticket details.
 * Usage: java -jar app.jar [TICKET_KEY]
 * Example: java -jar app.jar AI-6
 * If no ticket key is provided, defaults to AI-6.
 */
@Component
public class JiraTicketRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(JiraTicketRunner.class);
    private static final String DEFAULT_TICKET_KEY = "AIP-6";

    private final JiraTicketService jiraTicketService;

    public JiraTicketRunner(JiraTicketService jiraTicketService) {
        this.jiraTicketService = jiraTicketService;
    }

    @Override
    public void run(String... args) {
        String ticketKey = args.length > 0 ? args[0] : DEFAULT_TICKET_KEY;
        
        logger.info("Fetching Jira ticket: {}", ticketKey);
        
        jiraTicketService.getTicket(ticketKey)
                .doOnSuccess(ticket -> {
                    printTicketDetails(ticket);
                })
                .doOnError(error -> {
                    logger.error("Failed to fetch ticket {}: {}", ticketKey, error.getMessage(), error);
                    System.err.println("\n❌ Error fetching ticket: " + error.getMessage());
                    if (error.getCause() != null) {
                        System.err.println("Cause: " + error.getCause().getMessage());
                    }
                })
                .block(); // Block to wait for the result in CommandLineRunner
    }

    private void printTicketDetails(JiraTicket ticket) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("JIRA TICKET DETAILS");
        System.out.println("=".repeat(80));
        
        System.out.println("\n📋 Basic Information:");
        System.out.println("  Key:        " + ticket.getKey());
        System.out.println("  ID:         " + ticket.getId());
        System.out.println("  URL:        " + ticket.getSelf());
        
        if (ticket.getFields() != null) {
            JiraTicket.Fields fields = ticket.getFields();
            
            System.out.println("\n📝 Summary:");
            System.out.println("  " + (fields.getSummary() != null ? fields.getSummary() : "N/A"));
            
            System.out.println("\n📄 Description:");
            String description = fields.getDescriptionAsString();
            if (description != null && !description.trim().isEmpty()) {
                // Print description with indentation, handling multi-line
                String[] lines = description.split("\n");
                for (String line : lines) {
                    System.out.println("  " + line);
                }
            } else {
                System.out.println("  N/A");
            }
            
            System.out.println("\n📊 Status & Type:");
            if (fields.getStatus() != null) {
                System.out.println("  Status:     " + fields.getStatus().getName());
            }
            if (fields.getIssueType() != null) {
                System.out.println("  Issue Type: " + fields.getIssueType().getName());
            }
            if (fields.getPriority() != null) {
                System.out.println("  Priority:   " + fields.getPriority().getName());
            }
            
            System.out.println("\n👥 People:");
            if (fields.getAssignee() != null) {
                System.out.println("  Assignee:   " + fields.getAssignee().getDisplayName());
                if (fields.getAssignee().getEmailAddress() != null) {
                    System.out.println("              " + fields.getAssignee().getEmailAddress());
                }
            } else {
                System.out.println("  Assignee:   Unassigned");
            }
            
            if (fields.getReporter() != null) {
                System.out.println("  Reporter:   " + fields.getReporter().getDisplayName());
                if (fields.getReporter().getEmailAddress() != null) {
                    System.out.println("              " + fields.getReporter().getEmailAddress());
                }
            }
            
            System.out.println("\n📦 Project:");
            if (fields.getProject() != null) {
                System.out.println("  Key:        " + fields.getProject().getKey());
                System.out.println("  Name:       " + fields.getProject().getName());
            }
            
            System.out.println("\n📅 Dates:");
            if (fields.getCreated() != null) {
                System.out.println("  Created:    " + fields.getCreated());
            }
            if (fields.getUpdated() != null) {
                System.out.println("  Updated:    " + fields.getUpdated());
            }
        }
        
        System.out.println("\n" + "=".repeat(80));
        System.out.println("✅ Ticket details retrieved successfully!");
        System.out.println("=".repeat(80) + "\n");
    }
}

