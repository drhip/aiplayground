package com.aiplayground.mcpjira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Model class representing a Jira ticket/issue.
 * Maps to the Jira REST API v3 issue response structure.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraTicket {

    @JsonProperty("id")
    private String id;

    @JsonProperty("key")
    private String key;

    @JsonProperty("self")
    private String self;

    @JsonProperty("fields")
    private Fields fields;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public Fields getFields() {
        return fields;
    }

    public void setFields(Fields fields) {
        this.fields = fields;
    }

    // Convenience methods to access common fields
    public String getSummary() {
        return fields != null ? fields.getSummary() : null;
    }

    public String getDescription() {
        if (fields == null || fields.getDescription() == null) {
            return null;
        }
        return fields.getDescriptionAsString();
    }

    public String getStatus() {
        return fields != null && fields.getStatus() != null ? fields.getStatus().getName() : null;
    }

    public String getAssignee() {
        return fields != null && fields.getAssignee() != null ? fields.getAssignee().getDisplayName() : null;
    }

    /**
     * Inner class representing the fields object in Jira API response.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        @JsonProperty("summary")
        private String summary;

        @JsonProperty("description")
        private JsonNode description;

        @JsonProperty("status")
        private Status status;

        @JsonProperty("assignee")
        private User assignee;

        @JsonProperty("reporter")
        private User reporter;

        @JsonProperty("project")
        private Project project;

        @JsonProperty("created")
        private String created;

        @JsonProperty("updated")
        private String updated;

        @JsonProperty("issuetype")
        private IssueType issueType;

        @JsonProperty("priority")
        private Priority priority;

        // Getters and Setters
        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public JsonNode getDescription() {
            return description;
        }

        public void setDescription(JsonNode description) {
            this.description = description;
        }

        /**
         * Extracts plain text from the description field.
         * In Jira API v3, description is an ADF (Atlassian Document Format) object.
         * This method attempts to extract readable text from it.
         */
        public String getDescriptionAsString() {
            if (description == null) {
                return null;
            }
            
            // If it's a text node, return it directly
            if (description.isTextual()) {
                return description.asText();
            }
            
            // If it's an object (ADF format), try to extract text content
            if (description.isObject()) {
                // Try to extract text from ADF structure
                // ADF typically has a "content" array with text nodes
                JsonNode content = description.get("content");
                if (content != null && content.isArray()) {
                    StringBuilder text = new StringBuilder();
                    extractTextFromContent(content, text);
                    return text.toString().trim();
                }
                
                // Fallback: return JSON string representation
                return description.toString();
            }
            
            // For other types, return as string
            return description.asText();
        }

        /**
         * Recursively extracts text from ADF content structure.
         */
        private void extractTextFromContent(JsonNode content, StringBuilder text) {
            if (content.isArray()) {
                for (JsonNode node : content) {
                    extractTextFromContent(node, text);
                }
            } else if (content.isObject()) {
                // Check for text node
                JsonNode textNode = content.get("text");
                if (textNode != null && textNode.isTextual()) {
                    text.append(textNode.asText());
                    text.append(" ");
                }
                
                // Recursively process content array
                JsonNode nodeContent = content.get("content");
                if (nodeContent != null) {
                    extractTextFromContent(nodeContent, text);
                }
            }
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public User getAssignee() {
            return assignee;
        }

        public void setAssignee(User assignee) {
            this.assignee = assignee;
        }

        public User getReporter() {
            return reporter;
        }

        public void setReporter(User reporter) {
            this.reporter = reporter;
        }

        public Project getProject() {
            return project;
        }

        public void setProject(Project project) {
            this.project = project;
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }

        public String getUpdated() {
            return updated;
        }

        public void setUpdated(String updated) {
            this.updated = updated;
        }

        public IssueType getIssueType() {
            return issueType;
        }

        public void setIssueType(IssueType issueType) {
            this.issueType = issueType;
        }

        public Priority getPriority() {
            return priority;
        }

        public void setPriority(Priority priority) {
            this.priority = priority;
        }
    }

    /**
     * Inner class representing the status object.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Inner class representing a user (assignee, reporter).
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        @JsonProperty("accountId")
        private String accountId;

        @JsonProperty("displayName")
        private String displayName;

        @JsonProperty("emailAddress")
        private String emailAddress;

        @JsonProperty("active")
        private Boolean active;

        // Getters and Setters
        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEmailAddress() {
            return emailAddress;
        }

        public void setEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
        }

        public Boolean getActive() {
            return active;
        }

        public void setActive(Boolean active) {
            this.active = active;
        }
    }

    /**
     * Inner class representing the project object.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Project {
        @JsonProperty("id")
        private String id;

        @JsonProperty("key")
        private String key;

        @JsonProperty("name")
        private String name;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Inner class representing the issue type.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueType {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("description")
        private String description;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    /**
     * Inner class representing the priority.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Priority {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

