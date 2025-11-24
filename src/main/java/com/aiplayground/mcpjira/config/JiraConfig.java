package com.aiplayground.mcpjira.config;

import com.aiplayground.mcpjira.auth.JiraAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;
import io.netty.channel.ChannelOption;

import java.time.Duration;

/**
 * Configuration class to enable Jira configuration properties and HTTP client.
 * This makes JiraConfigProperties available as a Spring bean and configures WebClient
 * with authentication, timeouts, and retry logic.
 */
@Configuration
@EnableConfigurationProperties(JiraConfigProperties.class)
public class JiraConfig {

    private final JiraConfigProperties jiraConfigProperties;
    private final JiraAuthenticationService authenticationService;

    @Autowired
    public JiraConfig(JiraConfigProperties jiraConfigProperties, 
                     JiraAuthenticationService authenticationService) {
        this.jiraConfigProperties = jiraConfigProperties;
        this.authenticationService = authenticationService;
    }

    /**
     * Creates and configures a WebClient bean for Jira REST API communication.
     * 
     * Configuration includes:
     * - Base URL from JiraConfigProperties
     * - API token authentication (email + token) using Basic Auth via JiraAuthenticationService
     * - Connection timeout: 30 seconds (from AIP-77 decision)
     * - Read timeout: 60 seconds (from AIP-77 decision)
     * - Retry logic: 3 retries with exponential backoff (multiplier 2.0) (from AIP-77 decision)
     * 
     * @return Configured WebClient instance
     */
    @Bean
    public WebClient jiraWebClient() {
        // Create HTTP client with timeouts
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(jiraConfigProperties.getReadTimeout()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 
                        (int) Duration.ofSeconds(jiraConfigProperties.getConnectionTimeout()).toMillis());

        // Use authentication service to get auth header
        String authHeader = authenticationService.getAuthHeader();

        return WebClient.builder()
                .baseUrl(jiraConfigProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, authHeader)
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    /**
     * Creates a retry specification for WebClient requests.
     * 
     * Retry configuration (from AIP-77 decision):
     * - Maximum retries: 3
     * - Exponential backoff multiplier: 2.0
     * - Retries on: 5xx server errors, network/connection errors
     * 
     * @return RetryBackoffSpec for use with WebClient requests
     */
    @Bean
    public RetryBackoffSpec jiraRetrySpec() {
        return Retry.backoff(jiraConfigProperties.getMaxRetries(), Duration.ofSeconds(1))
                .multiplier(jiraConfigProperties.getRetryBackoffMultiplier())
                .filter(throwable -> {
                    // Retry on network errors and 5xx server errors
                    if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException) {
                        org.springframework.web.reactive.function.client.WebClientResponseException ex = 
                                (org.springframework.web.reactive.function.client.WebClientResponseException) throwable;
                        return ex.getStatusCode().is5xxServerError();
                    }
                    // Retry on network/connection errors
                    return throwable instanceof java.net.ConnectException 
                            || throwable instanceof java.net.SocketTimeoutException
                            || throwable instanceof java.io.IOException;
                });
    }
}

