package com.aiplayground.mcpjira.service;

import com.aiplayground.mcpjira.client.JiraHttpClient;
import com.aiplayground.mcpjira.model.JiraTicket;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for JiraTicketService.
 */
@ExtendWith(MockitoExtension.class)
class JiraTicketServiceTest {

    @Mock
    private JiraHttpClient jiraHttpClient;

    @InjectMocks
    private JiraTicketService jiraTicketService;

    private JiraTicket sampleTicket;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Create a sample ticket for testing
        sampleTicket = new JiraTicket();
        sampleTicket.setId("12345");
        sampleTicket.setKey("AI-6");
        sampleTicket.setSelf("https://example.atlassian.net/rest/api/3/issue/12345");

        JiraTicket.Fields fields = new JiraTicket.Fields();
        fields.setSummary("Test Ticket Summary");
        // Create a simple text JsonNode for description
        JsonNode descriptionNode = objectMapper.valueToTree("This is a test ticket description");
        fields.setDescription(descriptionNode);

        JiraTicket.Status status = new JiraTicket.Status();
        status.setId("10001");
        status.setName("To Do");
        fields.setStatus(status);

        JiraTicket.User assignee = new JiraTicket.User();
        assignee.setAccountId("123456:abcdef-1234-5678-90ab-cdef12345678");
        assignee.setDisplayName("John Doe");
        assignee.setEmailAddress("john.doe@example.com");
        fields.setAssignee(assignee);

        JiraTicket.Project project = new JiraTicket.Project();
        project.setId("10000");
        project.setKey("AI");
        project.setName("AI Project");
        fields.setProject(project);

        JiraTicket.IssueType issueType = new JiraTicket.IssueType();
        issueType.setId("10002");
        issueType.setName("Story");
        fields.setIssueType(issueType);

        fields.setCreated("2024-01-15T10:00:00.000+0000");
        fields.setUpdated("2024-01-16T14:30:00.000+0000");

        sampleTicket.setFields(fields);
    }

    @Test
    void testGetTicket_Success() {
        // Given
        String ticketKey = "AI-6";
        when(jiraHttpClient.get(eq("/rest/api/3/issue/" + ticketKey), eq(JiraTicket.class)))
                .thenReturn(Mono.just(sampleTicket));

        // When & Then
        StepVerifier.create(jiraTicketService.getTicket(ticketKey))
                .assertNext(ticket -> {
                    assertNotNull(ticket);
                    assertEquals("AI-6", ticket.getKey());
                    assertEquals("12345", ticket.getId());
                    assertEquals("Test Ticket Summary", ticket.getSummary());
                    assertEquals("This is a test ticket description", ticket.getDescription());
                    assertEquals("To Do", ticket.getStatus());
                    assertEquals("John Doe", ticket.getAssignee());
                    assertNotNull(ticket.getFields());
                    assertNotNull(ticket.getFields().getProject());
                    assertEquals("AI", ticket.getFields().getProject().getKey());
                })
                .verifyComplete();
    }

    @Test
    void testGetTicket_WithNullKey() {
        // When & Then
        StepVerifier.create(jiraTicketService.getTicket(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testGetTicket_WithEmptyKey() {
        // When & Then
        StepVerifier.create(jiraTicketService.getTicket(""))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testGetTicket_WithWhitespaceKey() {
        // When & Then
        StepVerifier.create(jiraTicketService.getTicket("   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testGetTicket_TrimsWhitespace() {
        // Given
        String ticketKey = "  AI-6  ";
        when(jiraHttpClient.get(eq("/rest/api/3/issue/AI-6"), eq(JiraTicket.class)))
                .thenReturn(Mono.just(sampleTicket));

        // When & Then
        StepVerifier.create(jiraTicketService.getTicket(ticketKey))
                .assertNext(ticket -> {
                    assertNotNull(ticket);
                    assertEquals("AI-6", ticket.getKey());
                })
                .verifyComplete();
    }

    @Test
    void testGetTicket_ApiError() {
        // Given
        String ticketKey = "AI-999";
        RuntimeException apiException = new RuntimeException("Ticket not found");
        when(jiraHttpClient.get(eq("/rest/api/3/issue/" + ticketKey), eq(JiraTicket.class)))
                .thenReturn(Mono.error(apiException));

        // When & Then
        StepVerifier.create(jiraTicketService.getTicket(ticketKey))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void testGetTicket_ConvenienceMethods() {
        // Given
        String ticketKey = "AI-6";
        when(jiraHttpClient.get(eq("/rest/api/3/issue/" + ticketKey), eq(JiraTicket.class)))
                .thenReturn(Mono.just(sampleTicket));

        // When & Then
        StepVerifier.create(jiraTicketService.getTicket(ticketKey))
                .assertNext(ticket -> {
                    // Test convenience methods
                    assertEquals("Test Ticket Summary", ticket.getSummary());
                    assertEquals("This is a test ticket description", ticket.getDescription());
                    assertEquals("To Do", ticket.getStatus());
                    assertEquals("John Doe", ticket.getAssignee());
                })
                .verifyComplete();
    }

    @Test
    void testGetTicket_WithNullFields() {
        // Given
        JiraTicket ticketWithNullFields = new JiraTicket();
        ticketWithNullFields.setId("12345");
        ticketWithNullFields.setKey("AI-6");
        ticketWithNullFields.setFields(null);

        String ticketKey = "AI-6";
        when(jiraHttpClient.get(eq("/rest/api/3/issue/" + ticketKey), eq(JiraTicket.class)))
                .thenReturn(Mono.just(ticketWithNullFields));

        // When & Then
        StepVerifier.create(jiraTicketService.getTicket(ticketKey))
                .assertNext(ticket -> {
                    assertNotNull(ticket);
                    assertEquals("AI-6", ticket.getKey());
                    assertNull(ticket.getFields());
                    // Convenience methods should handle null fields gracefully
                    assertNull(ticket.getSummary());
                    assertNull(ticket.getDescription());
                    assertNull(ticket.getStatus());
                    assertNull(ticket.getAssignee());
                })
                .verifyComplete();
    }
}

