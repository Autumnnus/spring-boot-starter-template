package com.autumnus.spring_boot_starter_template.common.email;

import com.autumnus.spring_boot_starter_template.common.config.EmailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailProperties emailProperties;

    @Override
    @Async
    public void sendVerificationEmail(String to, String username, String verificationToken) {
        String verificationUrl = emailProperties.getBaseUrl() + "/api/v1/auth/verify-email?token=" + verificationToken;

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("verificationUrl", verificationUrl);

        String htmlContent = templateEngine.process("email/verification", context);

        sendEmail(to, "Verify Your Email Address", htmlContent);
        log.info("Verification email sent to: {}", to);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        String resetUrl = emailProperties.getBaseUrl() + "/api/v1/auth/reset-password?token=" + resetToken;

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("resetUrl", resetUrl);

        String htmlContent = templateEngine.process("email/password-reset", context);

        sendEmail(to, "Reset Your Password", htmlContent);
        log.info("Password reset email sent to: {}", to);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);

        String htmlContent = templateEngine.process("email/welcome", context);

        sendEmail(to, "Welcome to Spring Boot Starter!", htmlContent);
        log.info("Welcome email sent to: {}", to);
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);

        String htmlContent = templateEngine.process("email/password-changed", context);

        sendEmail(to, "Password Changed Successfully", htmlContent);
        log.info("Password changed email sent to: {}", to);
    }

    @Override
    @Async
    public void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(emailProperties.getFrom(), emailProperties.getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.debug("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to: {}, error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}, error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
