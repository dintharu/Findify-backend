package edu.icet.ecom.controller;

import edu.icet.ecom.model.dto.ClaimRequestDto;
import edu.icet.ecom.model.dto.ClaimedItemDto;
import edu.icet.ecom.service.ClaimedItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin(origins = "*")
public class ClaimedItemController {

    @Autowired
    private ClaimedItemService claimedItemService;

    /**
     * Process a new claim request
     */
    @PostMapping("/process")
    public ResponseEntity<?> processClaim(@RequestBody ClaimRequestDto claimRequest) {
        try {
            // Validate required fields
            if (claimRequest.getLostItemId() == null || claimRequest.getLostItemId().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Lost item ID is required");
            }
            if (claimRequest.getClaimerName() == null || claimRequest.getClaimerName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Claimer name is required");
            }
            if (claimRequest.getClaimerEmail() == null || claimRequest.getClaimerEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Claimer email is required");
            }
            if (claimRequest.getClaimReason() == null || claimRequest.getClaimReason().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Claim reason is required");
            }

            ClaimedItemDto result = claimedItemService.processClaim(claimRequest);
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error processing claim: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error while processing claim");
        }
    }

    /**
     * Get all active claims for a user
     */
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<ClaimedItemDto>> getClaimsForUser(@PathVariable String userEmail) {
        try {
            List<ClaimedItemDto> claims = claimedItemService.getActiveClaimsForUser(userEmail);
            return ResponseEntity.ok(claims);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all claims made by a specific claimer
     */
    @GetMapping("/claimer/{claimerEmail}")
    public ResponseEntity<List<ClaimedItemDto>> getClaimsByClaimerEmail(@PathVariable String claimerEmail) {
        try {
            List<ClaimedItemDto> claims = claimedItemService.getClaimsByClaimerEmail(claimerEmail);
            return ResponseEntity.ok(claims);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get specific claim details
     */
    @GetMapping("/{claimId}")
    public ResponseEntity<?> getClaimById(@PathVariable Long claimId) {
        try {
            ClaimedItemDto claim = claimedItemService.getClaimById(claimId);
            return ResponseEntity.ok(claim);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Manually resolve a claim
     */
    @PutMapping("/{claimId}/resolve")
    public ResponseEntity<String> resolveClaim(@PathVariable Long claimId) {
        try {
            claimedItemService.resolveClaim(claimId);
            return ResponseEntity.ok("Claim resolved successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error resolving claim");
        }
    }

    /**
     * Check if an item is already claimed
     */
    @GetMapping("/check/{itemId}")
    public ResponseEntity<Boolean> isItemClaimed(@PathVariable String itemId) {
        try {
            boolean isClaimed = claimedItemService.isItemAlreadyClaimed(itemId);
            return ResponseEntity.ok(isClaimed);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get claim statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<ClaimedItemService.ClaimStatistics> getClaimStatistics() {
        try {
            ClaimedItemService.ClaimStatistics stats = claimedItemService.getClaimStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Trigger manual cleanup of expired claims (for admin use)
     */
    @PostMapping("/cleanup")
    public ResponseEntity<String> manualCleanup() {
        try {
            claimedItemService.cleanupExpiredClaims();
            return ResponseEntity.ok("Expired claims cleanup completed");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during cleanup: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Claimed Item Service is running");
    }


    @GetMapping("/all")
    public ResponseEntity<List<ClaimedItemDto>> getAllActiveClaims() {
        try {
            // Get all active claims - you can modify the service method
            List<ClaimedItemDto> claims = claimedItemService.getAllActiveClaims();
            return ResponseEntity.ok(claims);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}