package edu.icet.ecom.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "email-service",
        url = "http://localhost:8085",
        fallback = EmailServiceClientFallback.class
)
public interface EmailServiceClient {

    @GetMapping("/api/notifications/health")
    ResponseEntity<String> healthCheck();

    @PostMapping("/api/notifications/item-matched")
    ResponseEntity<String> sendItemMatchedNotification(
            @RequestParam Long userId,
            @RequestParam String email,
            @RequestParam String itemName,
            @RequestParam String matchDetails);

    @PostMapping("/api/notifications/claim-request")
    ResponseEntity<String> sendClaimRequestNotification(
            @RequestParam Long userId,
            @RequestParam String email,
            @RequestParam String itemName,
            @RequestParam String requesterName);

    @PostMapping("/api/notifications/welcome")
    ResponseEntity<String> sendWelcomeEmail(
            @RequestParam Long userId,
            @RequestParam String email,
            @RequestParam String userName);
}