package edu.icet.ecom.service.custom.Impl;

import edu.icet.ecom.enums.NotificationStatus;
import edu.icet.ecom.enums.NotificationType;
import edu.icet.ecom.model.DTO.request.NotificationRequest;
import edu.icet.ecom.model.DTO.response.NotificationResponse;
import edu.icet.ecom.model.entity.NotificationEntity;
import edu.icet.ecom.repository.NotificationRepository;
import edu.icet.ecom.service.custom.EmailService;
import edu.icet.ecom.service.custom.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Override
    public void sendNotification(NotificationRequest notificationRequest) {
        try {
            NotificationEntity notificationEntity = new NotificationEntity();
            notificationEntity.setUserId(notificationRequest.getUserId());
            notificationEntity.setEmail(notificationRequest.getEmail());
            notificationEntity.setSubject(notificationRequest.getSubject());
            notificationEntity.setMessage(notificationRequest.getMessage());
            notificationEntity.setType(notificationRequest.getType());
            notificationEntity.setStatus(NotificationStatus.PENDING);

            NotificationEntity savedNotification = notificationRepository.save(notificationEntity);
            emailService.processNotification(savedNotification);

            log.info("Notification sent successfully to user: {}", notificationRequest.getUserId());
        } catch (Exception e) {
            log.error("Failed to send notification to user: {}. Error: {}",
                    notificationRequest.getUserId(), e.getMessage());
        }
    }

    @Override
    public List<NotificationResponse> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public List<NotificationResponse> getPendingNotifications(Long userId) {
        return notificationRepository.findByStatusOrderByCreatedAtDesc(NotificationStatus.PENDING)
                .stream()
                .filter(notification -> notification.getUserId().equals(userId))
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void retryFailedNotifications() {
        try {
            LocalDateTime retryTime = LocalDateTime.now().minusMinutes(15);
            List<NotificationEntity> failedNotifications =
                    notificationRepository.findFailedNotificationsForRetry(retryTime);

            log.info("Retrying {} failed notifications", failedNotifications.size());

            for (NotificationEntity notification : failedNotifications) {
                try {
                    notification.setStatus(NotificationStatus.PENDING);
                    emailService.processNotification(notification);
                    log.info("Retried notification ID: {}", notification.getId());
                } catch (Exception e) {
                    log.error("Failed to retry notification ID: {}. Error: {}",
                            notification.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Error during failed notifications retry: {}", e.getMessage());
        }
    }

    @Override
    public void sendItemMatchedNotification(Long userId, String email, String itemName, String matchDetails) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setEmail(email);
        request.setSubject("Item Match Found!");
        request.setMessage("Great news! We found a potential match for your item: " + itemName + ". " + matchDetails);
        request.setType(NotificationType.ITEM_MATCHED);

        sendNotification(request);
    }

    @Override
    public void sendClaimRequestNotification(Long userId, String email, String itemName, String requesterName) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setEmail(email);
        request.setSubject("New Claim Request");
        request.setMessage("Someone has requested to claim your found item: " + itemName + ". Requester: " + requesterName);
        request.setType(NotificationType.CLAIM_REQUEST);

        sendNotification(request);
    }

    @Override
    public void sendWelcomeEmail(Long userId, String email, String userName) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setEmail(email);
        request.setSubject("Welcome to Findify!");
        request.setMessage("Welcome " + userName + "! Thank you for joining Findify Lost & Found system.");
        request.setType(NotificationType.WELCOME_EMAIL);

        sendNotification(request);
    }

    @Override
    public NotificationResponse mapToResponse(NotificationEntity entity) {
        NotificationResponse response = new NotificationResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setEmail(entity.getEmail());
        response.setSubject(entity.getSubject());
        response.setType(entity.getType());
        response.setStatus(entity.getStatus());
        response.setCreatedAt(entity.getCreatedAt());
        response.setSentAt(entity.getSentAt());
        return response;
    }
}