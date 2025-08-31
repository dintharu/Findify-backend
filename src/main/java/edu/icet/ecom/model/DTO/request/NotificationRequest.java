package edu.icet.ecom.model.DTO.request;

import edu.icet.ecom.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationRequest {
    private Long userId;
    private String email;
    private String subject;
    private String message;
    private NotificationType type;
    private Map<String, String> templateData;
}
