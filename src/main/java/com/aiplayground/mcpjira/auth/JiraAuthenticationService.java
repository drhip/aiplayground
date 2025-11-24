package com.aiplayground.mcpjira.auth;

import com.aiplayground.mcpjira.config.JiraConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Authentication service for Jira REST API.
 * 
 * This service provides API token authentication support using email and API token.
 * It manages authentication headers and handles authentication-related errors.
 * 
 * Features:
 * - API token authentication (email + token) via Basic Auth
 * - Authentication header generation and management
 * - Authentication error handling and validation
 */
@Service
public class JiraAuthenticationService {

    private final JiraConfigProperties jiraConfigProperties;
    private final String authHeader;

    @Autowired
    public JiraAuthenticationService(JiraConfigProperties jiraConfigProperties) {
        this.jiraConfigProperties = jiraConfigProperties;
        validateAuthenticationCredentials();
        this.authHeader = generateAuthHeader();
    }

    /**
     * Gets the Authorization header value for Jira API requests.
     * 
     * @return Basic Auth header value (e.g., "Basic base64encodedcredentials")
     */
    public String getAuthHeader() {
        return authHeader;
    }

    /**
     * Gets the email used for authentication.
     * 
     * @return Email address
     */
    public String getEmail() {
        return jiraConfigProperties.getEmail();
    }

    /**
     * Checks if authentication credentials are valid.
     * 
     * @return true if credentials are valid, false otherwise
     */
    public boolean isValid() {
        return jiraConfigProperties.getEmail() != null 
                && !jiraConfigProperties.getEmail().isBlank()
                && jiraConfigProperties.getApiToken() != null
                && !jiraConfigProperties.getApiToken().isBlank();
    }

    /**
     * Generates the Basic Auth header from email and API token.
     * 
     * Format: "Basic base64(email:token)"
     * 
     * @return Basic Auth header value
     * @throws AuthenticationException if credentials are invalid
     */
    private String generateAuthHeader() {
        if (!isValid()) {
            throw new AuthenticationException(
                "Invalid authentication credentials. Email and API token are required."
            );
        }

        String credentials = jiraConfigProperties.getEmail() + ":" + jiraConfigProperties.getApiToken();
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }

    /**
     * Validates that authentication credentials are present and not empty.
     * 
     * @throws AuthenticationException if credentials are missing or invalid
     */
    private void validateAuthenticationCredentials() {
        if (jiraConfigProperties.getEmail() == null || jiraConfigProperties.getEmail().isBlank()) {
            throw new AuthenticationException(
                "Jira email is required for authentication. " +
                "Please set JIRA_EMAIL environment variable or jira.email property."
            );
        }

        if (jiraConfigProperties.getApiToken() == null || jiraConfigProperties.getApiToken().isBlank()) {
            throw new AuthenticationException(
                "Jira API token is required for authentication. " +
                "Please set JIRA_API_TOKEN environment variable or jira.api-token property."
            );
        }
    }

    /**
     * Exception thrown when authentication fails or credentials are invalid.
     */
    public static class AuthenticationException extends RuntimeException {
        public AuthenticationException(String message) {
            super(message);
        }

        public AuthenticationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

