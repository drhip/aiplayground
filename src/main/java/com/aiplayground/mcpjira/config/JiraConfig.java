package com.aiplayground.mcpjira.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to enable Jira configuration properties.
 * This makes JiraConfigProperties available as a Spring bean.
 */
@Configuration
@EnableConfigurationProperties(JiraConfigProperties.class)
public class JiraConfig {
}

