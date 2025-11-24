package com.aiplayground.mcpjira.client;

import com.aiplayground.mcpjira.auth.JiraAuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.RetryBackoffSpec;

/**
 * HTTP client wrapper for Jira REST API communication.
 * 
 * This class provides a simplified interface for making HTTP requests to the Jira API
 * using Spring WebClient with configured authentication, timeouts, and retry logic.
 * 
 * Features:
 * - API token authentication (email + token) via Basic Auth
 * - Connection timeout: 30 seconds
 * - Read timeout: 60 seconds
 * - Retry logic: 3 retries with exponential backoff (multiplier 2.0)
 * - Error handling with appropriate exception mapping including authentication errors
 */
@Component
public class JiraHttpClient {

    private final WebClient webClient;
    private final RetryBackoffSpec retrySpec;
    private final JiraAuthenticationService authenticationService;

    @Autowired
    public JiraHttpClient(@Qualifier("jiraWebClient") WebClient webClient,
                         @Qualifier("jiraRetrySpec") RetryBackoffSpec retrySpec,
                         JiraAuthenticationService authenticationService) {
        this.webClient = webClient;
        this.retrySpec = retrySpec;
        this.authenticationService = authenticationService;
    }

    /**
     * Performs a GET request to the specified path.
     * 
     * @param path The API path (relative to base URL)
     * @param responseType The class type of the response
     * @param <T> The response type
     * @return Mono containing the response object
     * @throws JiraHttpClientException if the request fails
     */
    public <T> Mono<T> get(String path, Class<T> responseType) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(retrySpec)
                .onErrorMap(this::mapToJiraHttpClientException);
    }

    /**
     * Performs a GET request to the specified path and returns the raw response body as String.
     * 
     * @param path The API path (relative to base URL)
     * @return Mono containing the response body as String
     * @throws JiraHttpClientException if the request fails
     */
    public Mono<String> get(String path) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(retrySpec)
                .onErrorMap(this::mapToJiraHttpClientException);
    }

    /**
     * Performs a POST request to the specified path with a request body.
     * 
     * @param path The API path (relative to base URL)
     * @param body The request body object
     * @param responseType The class type of the response
     * @param <T> The request body type
     * @param <R> The response type
     * @return Mono containing the response object
     * @throws JiraHttpClientException if the request fails
     */
    public <T, R> Mono<R> post(String path, T body, Class<R> responseType) {
        return webClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(retrySpec)
                .onErrorMap(this::mapToJiraHttpClientException);
    }

    /**
     * Performs a POST request to the specified path with a request body and returns the raw response body as String.
     * 
     * @param path The API path (relative to base URL)
     * @param body The request body object
     * @param <T> The request body type
     * @return Mono containing the response body as String
     * @throws JiraHttpClientException if the request fails
     */
    public <T> Mono<String> post(String path, T body) {
        return webClient.post()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(retrySpec)
                .onErrorMap(this::mapToJiraHttpClientException);
    }

    /**
     * Performs a PUT request to the specified path with a request body.
     * 
     * @param path The API path (relative to base URL)
     * @param body The request body object
     * @param responseType The class type of the response
     * @param <T> The request body type
     * @param <R> The response type
     * @return Mono containing the response object
     * @throws JiraHttpClientException if the request fails
     */
    public <T, R> Mono<R> put(String path, T body, Class<R> responseType) {
        return webClient.put()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(retrySpec)
                .onErrorMap(this::mapToJiraHttpClientException);
    }

    /**
     * Performs a PUT request to the specified path with a request body and returns the raw response body as String.
     * 
     * @param path The API path (relative to base URL)
     * @param body The request body object
     * @param <T> The request body type
     * @return Mono containing the response body as String
     * @throws JiraHttpClientException if the request fails
     */
    public <T> Mono<String> put(String path, T body) {
        return webClient.put()
                .uri(path)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .retryWhen(retrySpec)
                .onErrorMap(this::mapToJiraHttpClientException);
    }

    /**
     * Performs a DELETE request to the specified path.
     * 
     * @param path The API path (relative to base URL)
     * @return Mono containing Void (completes when request succeeds)
     * @throws JiraHttpClientException if the request fails
     */
    public Mono<Void> delete(String path) {
        return webClient.delete()
                .uri(path)
                .retrieve()
                .bodyToMono(Void.class)
                .retryWhen(retrySpec)
                .onErrorMap(this::mapToJiraHttpClientException);
    }

    /**
     * Performs a DELETE request to the specified path and returns the response body.
     * 
     * @param path The API path (relative to base URL)
     * @param responseType The class type of the response
     * @param <T> The response type
     * @return Mono containing the response object
     * @throws JiraHttpClientException if the request fails
     */
    public <T> Mono<T> delete(String path, Class<T> responseType) {
        return webClient.delete()
                .uri(path)
                .retrieve()
                .bodyToMono(responseType)
                .retryWhen(retrySpec)
                .onErrorMap(this::mapToJiraHttpClientException);
    }

    /**
     * Maps exceptions to JiraHttpClientException with appropriate error messages.
     * Handles authentication errors specifically (401 Unauthorized).
     * 
     * @param throwable The original exception
     * @return JiraHttpClientException with mapped error details
     */
    private JiraHttpClientException mapToJiraHttpClientException(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            HttpStatus statusCode = HttpStatus.resolve(ex.getStatusCode().value());
            
            // Handle authentication errors specifically
            if (statusCode == HttpStatus.UNAUTHORIZED) {
                String message = String.format(
                    "Jira API authentication failed (401 Unauthorized). " +
                    "Please verify your email (%s) and API token are correct. " +
                    "Response: %s",
                    authenticationService.getEmail(),
                    ex.getResponseBodyAsString()
                );
                return new JiraHttpClientException(message, ex, statusCode);
            }
            
            String message = String.format("Jira API request failed with status %d: %s", 
                    ex.getStatusCode().value(), ex.getResponseBodyAsString());
            return new JiraHttpClientException(message, ex, statusCode);
        }
        
        // Handle authentication service exceptions
        if (throwable instanceof JiraAuthenticationService.AuthenticationException) {
            return new JiraHttpClientException(
                "Jira authentication configuration error: " + throwable.getMessage(), 
                throwable
            );
        }
        
        return new JiraHttpClientException("Jira API request failed: " + throwable.getMessage(), throwable);
    }

    /**
     * Custom exception for Jira HTTP client errors.
     */
    public static class JiraHttpClientException extends RuntimeException {
        private final HttpStatus statusCode;

        public JiraHttpClientException(String message, Throwable cause) {
            super(message, cause);
            this.statusCode = null;
        }

        public JiraHttpClientException(String message, Throwable cause, HttpStatus statusCode) {
            super(message, cause);
            this.statusCode = statusCode;
        }

        public HttpStatus getStatusCode() {
            return statusCode;
        }
    }
}

