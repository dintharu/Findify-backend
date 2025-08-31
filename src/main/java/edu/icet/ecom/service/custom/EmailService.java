package edu.icet.ecom.service.custom;

import edu.icet.ecom.enums.NotificationType;
import edu.icet.ecom.model.entity.NotificationEntity;

import java.util.Map;

public interface EmailService {

    boolean sendSimpleEmail(String to, String subject, String text);

    boolean sendHtmlEmail(String to, String subject, String htmlContent);

    boolean sendTemplateEmail(String to, String subject, NotificationType type, Map<String, String> templateData);

    void processNotification(NotificationEntity notification);

    String getTemplateNameByType(NotificationType type);

    String generateHtmlContent(NotificationEntity notification);
}
