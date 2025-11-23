package com.aiplayground.mcpjira.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for Jira REST API client.
 * Supports both environment variables and application.properties configuration.
 * Environment variables take precedence over config file values.
 */
@ConfigurationProperties(prefix = "jira")
@Validated
public class JiraConfigProperties {

    /**
     * Jira base URL (required).
     * Can be set via JIRA_BASE_URL environment variable or jira.base-url property.
     */
    @NotBlank(message = "Jira base URL is required")
    private String baseUrl;

    /**
     * Email for API token authentication (required).
     * Can be set via JIRA_EMAIL environment variable or jira.email property.
     */
    @NotBlank(message = "Jira email is required")
    private String email;

    /**
     * API token for authentication (required).
     * Can be set via JIRA_API_TOKEN environment variable or jira.api-token property.
     */
    @NotBlank(message = "Jira API token is required")
    private String apiToken;

    /**
     * Connection timeout in seconds (optional, default: 30).
     * Can be set via JIRA_CONNECTION_TIMEOUT environment variable or jira.connection-timeout property.
     */
    private int connectionTimeout = 30;

    /**
     * Read timeout in seconds (optional, default: 60).
     * Can be set via JIRA_READ_TIMEOUT environment variable or jira.read-timeout property.
     */
    private int readTimeout = 60;

    /**
     * Maximum number of retries (optional, default: 3).
     * Can be set via JIRA_MAX_RETRIES environment variable or jira.max-retries property.
     */
    private int maxRetries = 3;

    /**
     * Exponential backoff multiplier for retries (optional, default: 2.0).
     * Can be set via JIRA_RETRY_BACKOFF_MULTIPLIER environment variable or jira.retry-backoff-multiplier property.
     */
    private double retryBackoffMultiplier = 2.0;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public double getRetryBackoffMultiplier() {
        return retryBackoffMultiplier;
    }

    public void setRetryBackoffMultiplier(double retryBackoffMultiplier) {
        this.retryBackoffMultiplier = retryBackoffMultiplier;
    }
}

