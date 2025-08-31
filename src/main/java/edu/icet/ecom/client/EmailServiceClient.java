// EmailServiceClient.java
package edu.icet.ecom.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "email-service", url = "http://localhost:8085")
public interface EmailServiceClient {

    @PostMapping("/api/email/claim-notification")
    ResponseEntity<String> sendClaimRequestNotification(
            @RequestParam Long userId,
            @RequestParam String userEmail,
            @RequestParam String itemTitle,
            @RequestParam String message);

    @PostMapping("/api/email/match-notification")
    ResponseEntity<String> sendItemMatchedNotification(
            @RequestParam Long userId,
            @RequestParam String userEmail,
            @RequestParam String itemTitle,
            @RequestParam String message);

    @GetMapping("/api/email/health")
    ResponseEntity<String> healthCheck();
}