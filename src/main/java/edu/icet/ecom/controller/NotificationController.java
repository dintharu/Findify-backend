package edu.icet.ecom.controller;

import edu.icet.ecom.model.DTO.request.NotificationRequest;
import edu.icet.ecom.model.DTO.response.NotificationResponse;
import edu.icet.ecom.service.custom.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    // Add health check endpoint that your EmailServiceClient is calling
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Email Service is running");
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationRequest notificationRequest) {
        try {
            notificationService.sendNotification(notificationRequest);
            return ResponseEntity.ok("Notification sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send notification: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@PathVariable Long userId) {
        List<NotificationResponse> notifications = notificationService.getUserNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/pending")
    public ResponseEntity<List<NotificationResponse>> getPendingNotifications(@PathVariable Long userId) {
        List<NotificationResponse> notifications = notificationService.getPendingNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/retry-failed")
    public ResponseEntity<String> retryFailedNotifications() {
        try {
            notificationService.retryFailedNotifications();
            return ResponseEntity.ok("Failed notifications retry initiated");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to retry notifications: " + e.getMessage());
        }
    }

    // Quick notification endpoints for automatic notifications
    @PostMapping("/item-matched")
    public ResponseEntity<String> sendItemMatchedNotification(
            @RequestParam Long userId,
            @RequestParam String email,
            @RequestParam String itemName,
            @RequestParam String matchDetails) {

        try {
            notificationService.sendItemMatchedNotification(userId, email, itemName, matchDetails);
            return ResponseEntity.ok("Item matched notification sent");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send notification: " + e.getMessage());
        }
    }

    @PostMapping("/claim-request")
    public ResponseEntity<String> sendClaimRequestNotification(
            @RequestParam Long userId,
            @RequestParam String email,
            @RequestParam String itemName,
            @RequestParam String requesterName) {

        try {
            notificationService.sendClaimRequestNotification(userId, email, itemName, requesterName);
            return ResponseEntity.ok("Claim request notification sent");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send notification: " + e.getMessage());
        }
    }


    @PostMapping("/welcome")
    public ResponseEntity<String> sendWelcomeEmail(
            @RequestParam Long userId,
            @RequestParam String email,
            @RequestParam String userName) {

        try {
            notificationService.sendWelcomeEmail(userId, email, userName);
            return ResponseEntity.ok("Welcome email sent");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send welcome email: " + e.getMessage());
        }
    }
}