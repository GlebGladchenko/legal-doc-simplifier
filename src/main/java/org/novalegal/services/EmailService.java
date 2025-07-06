package org.novalegal.services;

public interface EmailService {
    /**
     * Sends an email with the specified subject and text, setting the reply-to address to the user's email.
     *
     * @param subject   the subject of the email
     * @param text      the body content of the email
     * @param userEmail the email address of the user to be set as the reply-to address
     */
    void sendEmail(String subject, String text, String userEmail);
}