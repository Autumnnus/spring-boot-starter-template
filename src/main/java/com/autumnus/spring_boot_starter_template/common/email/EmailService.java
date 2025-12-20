package com.autumnus.spring_boot_starter_template.common.email;

public interface EmailService {

    void sendVerificationEmail(String to, String username, String verificationToken);

    void sendPasswordResetEmail(String to, String username, String resetToken);

    void sendWelcomeEmail(String to, String username);

    void sendPasswordChangedEmail(String to, String username);

    void sendEmail(String to, String subject, String htmlContent);
}
