package edu.icet.ecom.service.custom;

import edu.icet.ecom.model.DTO.request.NotificationRequest;
import edu.icet.ecom.model.DTO.response.NotificationResponse;
import edu.icet.ecom.model.entity.NotificationEntity;

import java.util.List;

public interface NotificationService {

    void sendNotification(NotificationRequest notificationRequest);

    List<NotificationResponse> getUserNotifications(Long userId);

    List<NotificationResponse> getPendingNotifications(Long userId);

    void retryFailedNotifications();

    void sendItemMatchedNotification(Long userId, String email, String itemName, String matchDetails);

    void sendClaimRequestNotification(Long userId, String email, String itemName, String requesterName);

    void sendWelcomeEmail(Long userId, String email, String userName);

    NotificationResponse mapToResponse(NotificationEntity entity);


}

