package com.kafnotif.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Email notification specific implementation
 */
public class EmailNotification extends NotificationEvent {
    
    @JsonProperty("subject")
    private String subject;
    
    @JsonProperty("body")
    private String body;
    
    @JsonProperty("htmlBody")
    private String htmlBody;
    
    @JsonProperty("cc")
    private List<String> cc;
    
    @JsonProperty("bcc")
    private List<String> bcc;
    
    @JsonProperty("attachments")
    private List<String> attachments;
    
    @JsonProperty("fromEmail")
    private String fromEmail;
    
    @JsonProperty("fromName")
    private String fromName;

    public EmailNotification() {
        super();
    }

    public EmailNotification(String recipient, String subject, String body) {
        super(NotificationType.EMAIL, recipient);
        this.subject = subject;
        this.body = body;
    }

    public EmailNotification(String recipient, String subject, String body, String htmlBody) {
        this(recipient, subject, body);
        this.htmlBody = htmlBody;
    }

    @Override
    public boolean isValid() {
        return getRecipient() != null && !getRecipient().trim().isEmpty() &&
               subject != null && !subject.trim().isEmpty() &&
               (body != null || htmlBody != null) &&
               isValidEmail(getRecipient());
    }

    @Override
    public String getContent() {
        return htmlBody != null ? htmlBody : body;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Getters and Setters
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public List<String> getCc() {
        return cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public List<String> getBcc() {
        return bcc;
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }

    public List<String> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }
}
