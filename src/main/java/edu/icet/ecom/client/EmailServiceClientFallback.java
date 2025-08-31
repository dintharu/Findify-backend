// Fallback implementation for when email service is down
package edu.icet.ecom.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class EmailServiceClientFallback implements EmailServiceClient {

    @Override
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Email service is currently unavailable");
    }

    @Override
    public ResponseEntity<String> sendItemMatchedNotification(Long userId, String email, String itemName, String matchDetails) {
        System.err.println("Email service unavailable - Match notification not sent for user: " + email);
        return ResponseEntity.ok("Email service temporarily unavailable");
    }

    @Override
    public ResponseEntity<String> sendClaimRequestNotification(Long userId, String email, String itemName, String requesterName) {
        System.err.println("Email service unavailable - Claim request notification not sent for user: " + email);
        return ResponseEntity.ok("Email service temporarily unavailable");
    }

    @Override
    public ResponseEntity<String> sendWelcomeEmail(Long userId, String email, String userName) {
        System.err.println("Email service unavailable - Welcome email not sent for user: " + email);
        return ResponseEntity.ok("Email service temporarily unavailable");
    }
}