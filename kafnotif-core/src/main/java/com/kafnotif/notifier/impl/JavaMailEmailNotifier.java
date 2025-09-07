package com.kafnotif.notifier.impl;

import com.kafnotif.model.EmailNotification;
import com.kafnotif.model.Event;
import com.kafnotif.notifier.EmailNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * JavaMail implementation of EmailNotifier
 */
public class JavaMailEmailNotifier implements EmailNotifier {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaMailEmailNotifier.class);
    
    private final Session session;
    private final String fromEmail;
    
    /**
     * Constructor with SMTP configuration
     */
    public JavaMailEmailNotifier(String smtpHost, int smtpPort, String username, String password, String fromEmail) {
        this.fromEmail = fromEmail;
        
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        
        // Only enable authentication if credentials are provided
        boolean hasCredentials = username != null && password != null && !username.trim().isEmpty() && !password.trim().isEmpty();
        props.put("mail.smtp.auth", String.valueOf(hasCredentials));
        props.put("mail.smtp.starttls.enable", "true");
        
        if (hasCredentials) {
            this.session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        } else {
            // No authentication - suitable for development servers like MailCatcher
            this.session = Session.getInstance(props);
        }
    }
    
    /**
     * Constructor with existing JavaMail Session
     */
    public JavaMailEmailNotifier(Session session, String fromEmail) {
        this.session = session;
        this.fromEmail = fromEmail;
    }
    
    @Override
    public boolean sendEmail(EmailNotification notification) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(notification.getRecipient()));
            message.setSubject(notification.getSubject());
            
            if (notification.getHtmlBody() != null) {
                message.setContent(notification.getHtmlBody(), "text/html; charset=utf-8");
            } else {
                message.setText(notification.getBody());
            }
            
            // Add CC recipients if present
            if (notification.getCc() != null && !notification.getCc().isEmpty()) {
                message.setRecipients(Message.RecipientType.CC, 
                    InternetAddress.parse(String.join(",", notification.getCc())));
            }
            
            // Add BCC recipients if present
            if (notification.getBcc() != null && !notification.getBcc().isEmpty()) {
                message.setRecipients(Message.RecipientType.BCC, 
                    InternetAddress.parse(String.join(",", notification.getBcc())));
            }
            
            Transport.send(message);
            logger.info("Email sent successfully to: {}", notification.getRecipient());
            return true;
            
        } catch (MessagingException e) {
            logger.error("Failed to send email to: {}", notification.getRecipient(), e);
            return false;
        }
    }
    
    @Override
    public boolean sendSimpleEmail(String to, String subject, String body) {
        EmailNotification notification = new EmailNotification();
        notification.setRecipient(to);
        notification.setSubject(subject);
        notification.setBody(body);
        notification.setHtmlBody(null);
        return sendEmail(notification);
    }
    
    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        EmailNotification notification = new EmailNotification();
        notification.setRecipient(to);
        notification.setSubject(subject);
        notification.setBody(htmlBody);
        notification.setHtmlBody(htmlBody);
        notification.setBody(null);
        return sendEmail(notification);
    }
    
    @Override
    public void send(Event event) {
        if (event instanceof EmailNotification) {
            sendEmail((EmailNotification) event);
        } else {
            throw new IllegalArgumentException("Event must be an EmailNotification");
        }
    }
}
