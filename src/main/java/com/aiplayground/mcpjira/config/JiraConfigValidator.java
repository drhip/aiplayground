package com.aiplayground.mcpjira.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validator component that ensures required Jira configuration is present on startup.
 * This provides fast-fail behavior with clear error messages if configuration is missing.
 */
@Component
public class JiraConfigValidator {

    private final JiraConfigProperties jiraConfigProperties;

    @Autowired
    public JiraConfigValidator(JiraConfigProperties jiraConfigProperties) {
        this.jiraConfigProperties = jiraConfigProperties;
    }

    @PostConstruct
    public void validateConfiguration() {
        if (jiraConfigProperties.getBaseUrl() == null || jiraConfigProperties.getBaseUrl().isBlank()) {
            throw new IllegalStateException(
                "Jira base URL is required. Please set JIRA_BASE_URL environment variable or jira.base-url property."
            );
        }

        if (jiraConfigProperties.getEmail() == null || jiraConfigProperties.getEmail().isBlank()) {
            throw new IllegalStateException(
                "Jira email is required. Please set JIRA_EMAIL environment variable or jira.email property."
            );
        }

        if (jiraConfigProperties.getApiToken() == null || jiraConfigProperties.getApiToken().isBlank()) {
            throw new IllegalStateException(
                "Jira API token is required. Please set JIRA_API_TOKEN environment variable or jira.api-token property."
            );
        }

        if (jiraConfigProperties.getConnectionTimeout() <= 0) {
            throw new IllegalStateException(
                "Jira connection timeout must be greater than 0. Current value: " + jiraConfigProperties.getConnectionTimeout()
            );
        }

        if (jiraConfigProperties.getReadTimeout() <= 0) {
            throw new IllegalStateException(
                "Jira read timeout must be greater than 0. Current value: " + jiraConfigProperties.getReadTimeout()
            );
        }

        if (jiraConfigProperties.getMaxRetries() < 0) {
            throw new IllegalStateException(
                "Jira max retries must be non-negative. Current value: " + jiraConfigProperties.getMaxRetries()
            );
        }

        if (jiraConfigProperties.getRetryBackoffMultiplier() <= 0) {
            throw new IllegalStateException(
                "Jira retry backoff multiplier must be greater than 0. Current value: " + jiraConfigProperties.getRetryBackoffMultiplier()
            );
        }
    }
}

