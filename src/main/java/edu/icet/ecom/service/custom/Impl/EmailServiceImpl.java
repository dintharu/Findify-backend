package edu.icet.ecom.service.custom.Impl;

import edu.icet.ecom.enums.NotificationStatus;
import edu.icet.ecom.enums.NotificationType;
import edu.icet.ecom.model.entity.NotificationEntity;
import edu.icet.ecom.repository.NotificationRepository;
import edu.icet.ecom.service.custom.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final NotificationRepository notificationRepository;

    @Override
    public boolean sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            simpleMailMessage.setFrom("laknathoki@gmail.com");
            simpleMailMessage.setTo(to);
            simpleMailMessage.setSubject(subject);
            simpleMailMessage.setText(text);

            javaMailSender.send(simpleMailMessage);
            log.info("Simple mail sent successfully to {}", to);
            return true;
        }catch(Exception e){
            log.error("Failed to send simple email to {}: {}", to, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("noreply@findify.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
            return true;
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            return false;
        }
    }

    @Override
    public boolean sendTemplateEmail(String to, String subject, NotificationType type, Map<String, String> templateData) {
        try {
            Context context = new Context();
            templateData.forEach(context::setVariable);

            String htmlContent = templateEngine.process(getTemplateNameByType(type), context);

            return sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            log.error("Failed to send template email to: {}", to, e);
            return false;
        }
    }

    @Override
    public void processNotification(NotificationEntity notification) {
        boolean success;

        if (notification.getType() == NotificationType.WELCOME_EMAIL ||
                notification.getType() == NotificationType.ITEM_MATCHED) {
            // Use template for these types
            success = sendHtmlEmail(
                    notification.getEmail(),
                    notification.getSubject(),
                    generateHtmlContent(notification)
            );
        } else {
            // Use simple email for others
            success = sendSimpleEmail(
                    notification.getEmail(),
                    notification.getSubject(),
                    notification.getMessage()
            );
        }

        // Update notification status
        if (success) {
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } else {
            notification.setStatus(NotificationStatus.FAILED);
        }

        notificationRepository.save(notification);
    }

    @Override
    public String getTemplateNameByType(NotificationType type) {
        switch (type) {
            case WELCOME_EMAIL:
                return "welcome-email";
            case ITEM_MATCHED:
                return "item-matched";
            case CLAIM_REQUEST:
                return "claim-request";
            case CLAIM_APPROVED:
                return "claim-approved";
            default:
                return "default-email";
        }
    }

    @Override
    public String generateHtmlContent(NotificationEntity notification) {
        // Simple HTML template generation
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>");
        html.append("<h2 style='color: #4CAF50;'>Findify - ").append(notification.getSubject()).append("</h2>");
        html.append("<p>").append(notification.getMessage()).append("</p>");
        html.append("<hr>");
        html.append("<p style='color: #666; font-size: 12px;'>This is an automated message from Findify Lost & Found System.</p>");
        html.append("</div>");
        html.append("</body></html>");
        return html.toString();
    }
}
