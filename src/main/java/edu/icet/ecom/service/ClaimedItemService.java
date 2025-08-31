package edu.icet.ecom.service;

import edu.icet.ecom.client.EmailServiceClient;
import edu.icet.ecom.client.ItemServiceClient;
import edu.icet.ecom.client.ImageTrackingServiceClient;
import edu.icet.ecom.enums.ClaimStatus;
import edu.icet.ecom.model.dto.ClaimRequestDto;
import edu.icet.ecom.model.dto.ClaimedItemDto;
import edu.icet.ecom.model.dto.ItemDto;
import edu.icet.ecom.model.dto.SimilarityResult;
import edu.icet.ecom.model.entity.ClaimedItem;
import edu.icet.ecom.repository.ClaimedItemRepository;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class ClaimedItemService {

    @Autowired
    private ClaimedItemRepository claimedItemRepository;

    @Autowired
    private ItemServiceClient itemServiceClient;

    @Autowired
    private ImageTrackingServiceClient imageTrackingServiceClient;

    @Autowired
    private EmailServiceClient emailServiceClient;

    @Value("${app.claim.similarity-threshold:0.65}")
    private double similarityThreshold;

    @Value("${app.claim.expiration-days:30}")
    private int expirationDays;

    /**
     * Process a claim request - finds matching items using image similarity and creates claim
     * This handles the core claim logic where users claim their lost items
     */
    // 3. Add comprehensive processClaim logging
    public ClaimedItemDto processClaim(ClaimRequestDto claimRequest) {
        try {
            log.info("=== STARTING CLAIM PROCESS ===");
            log.info("Request details: Lost={}, Claimer={}, Reason length={}",
                    claimRequest.getLostItemId(),
                    claimRequest.getClaimerEmail(),
                    claimRequest.getClaimReason().length());

            // Step 1: Validate
            validateClaimRequest(claimRequest);
            log.info("✓ Claim request validation passed");

            // Step 2: Fetch lost item
            ItemDto lostItem = fetchLostItem(claimRequest.getLostItemId());
            log.info("✓ Lost item fetched: {}", lostItem.getTitle());

            // Step 3: Check if already claimed
            validateItemNotClaimed(claimRequest.getLostItemId());
            log.info("✓ Item not previously claimed");

            // Step 4: Find matching found items
            List<ItemDto> matchingFoundItems = findSimilarFoundItems(lostItem);
            log.info("✓ Found {} potential matches", matchingFoundItems.size());

            if (matchingFoundItems.isEmpty()) {
                throw new RuntimeException("No matching found items detected for this lost item");
            }

            // Step 5: Get best match
            ItemDto bestFoundMatch = findBestMatch(lostItem, matchingFoundItems);
            log.info("✓ Best match selected: {}", bestFoundMatch.getTitle());

            // Step 6: Calculate final similarity
            SimilarityResult similarity = imageTrackingServiceClient.checkSimilarity(
                    lostItem.getImageUrl(), bestFoundMatch.getImageUrl());
            log.info("✓ Final similarity calculated: {}%", similarity.getSimilarityScore() * 100);

            // Step 7: Create claim record
            ClaimedItem claimedItem = createClaimedItemRecord(
                    lostItem, bestFoundMatch, claimRequest, similarity.getSimilarityScore());
            log.info("✓ Claim record created");

            // Step 8: Save to database
            ClaimedItem savedClaim = claimedItemRepository.save(claimedItem);
            log.info("✓ Claim saved to database with ID: {}", savedClaim.getId());

            // Step 9: Delete items from main system
            deleteItemsFromMainSystem(lostItem.getId(), bestFoundMatch.getId());
            log.info("✓ Items deletion attempted");

            // Step 10: Send notifications
            try {
                sendClaimNotifications(lostItem, bestFoundMatch, claimRequest, similarity.getSimilarityScore());
                log.info("✓ Notifications sent");
            } catch (Exception e) {
                log.warn("Notification sending failed, but claim processed successfully: {}", e.getMessage());
            }

            log.info("=== CLAIM PROCESS COMPLETED SUCCESSFULLY - ID: {} ===", savedClaim.getId());
            return mapToDto(savedClaim);

        } catch (Exception e) {
            log.error("=== CLAIM PROCESS FAILED ===", e);
            throw new RuntimeException("Failed to process claim: " + e.getMessage());
        }
    }

    /**
     * Get all active claimed items for a user (as owner of lost or found item)
     */
    public List<ClaimedItemDto> getActiveClaimsForUser(String userEmail) {
        try {
            List<ClaimedItem> claims = claimedItemRepository.findActiveClaimsByUserEmail(userEmail);
            return claims.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching claims for user: {}", userEmail, e);
            throw new RuntimeException("Failed to fetch claims for user");
        }
    }

    /**
     * Get all claims made by a specific claimer
     */
    public List<ClaimedItemDto> getClaimsByClaimerEmail(String claimerEmail) {
        try {
            List<ClaimedItem> claims = claimedItemRepository
                    .findByClaimerEmailAndClaimStatusOrderByClaimedAtDesc(claimerEmail, ClaimStatus.ACTIVE);
            return claims.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching claims by claimer: {}", claimerEmail, e);
            throw new RuntimeException("Failed to fetch claims by claimer");
        }
    }

    /**
     * Get claim details by ID
     */
    public ClaimedItemDto getClaimById(Long claimId) {
        try {
            ClaimedItem claim = claimedItemRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found with ID: " + claimId));
            return mapToDto(claim);
        } catch (Exception e) {
            log.error("Error fetching claim by ID: {}", claimId, e);
            throw new RuntimeException("Failed to fetch claim details");
        }
    }

    /**
     * Manual claim resolution (for admin or user confirmation)
     */
    public void resolveClaim(Long claimId) {
        try {
            ClaimedItem claim = claimedItemRepository.findById(claimId)
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            claim.setClaimStatus(ClaimStatus.RESOLVED);
            claimedItemRepository.save(claim);

            log.info("Claim resolved manually: {}", claimId);
        } catch (Exception e) {
            log.error("Error resolving claim: {}", claimId, e);
            throw new RuntimeException("Failed to resolve claim");
        }
    }

    /**
     * Check if an item is already claimed
     */
    public boolean isItemAlreadyClaimed(String itemId) {
        try {
            List<ClaimedItem> claims = claimedItemRepository.findActiveClaimsByItemId(itemId);
            return !claims.isEmpty();
        } catch (Exception e) {
            log.error("Error checking if item is claimed: {}", itemId, e);
            return false;
        }
    }

    /**
     * Get statistics about claims
     */
    public ClaimStatistics getClaimStatistics() {
        try {
            long activeClaims = claimedItemRepository.countByClaimStatus(ClaimStatus.ACTIVE);
            long expiredClaims = claimedItemRepository.countByClaimStatus(ClaimStatus.EXPIRED);
            long resolvedClaims = claimedItemRepository.countByClaimStatus(ClaimStatus.RESOLVED);

            return new ClaimStatistics(activeClaims, expiredClaims, resolvedClaims);
        } catch (Exception e) {
            log.error("Error fetching claim statistics", e);
            throw new RuntimeException("Failed to fetch claim statistics");
        }
    }

    /**
     * Scheduled method to clean up expired claims (runs daily at 2 AM)
     * This automatically handles the 1-month expiration you mentioned
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredClaims() {
        try {
            log.info("=== STARTING EXPIRED CLAIMS CLEANUP ===");
            LocalDateTime now = LocalDateTime.now();

            // Find and mark expired claims
            List<ClaimedItem> expiredClaims = claimedItemRepository.findExpiredClaims(now);

            if (expiredClaims.isEmpty()) {
                log.info("No expired claims found");
                return;
            }

            log.info("Found {} expired claims to process", expiredClaims.size());

            for (ClaimedItem claim : expiredClaims) {
                claim.setClaimStatus(ClaimStatus.EXPIRED);
                claimedItemRepository.save(claim);

                log.info("Marked claim as expired: {} (Lost: {})",
                        claim.getId(), claim.getLostItemTitle());
            }

            // Delete claims that have been expired for more than 7 days
            LocalDateTime deletionCutoff = now.minusDays(7);
            List<ClaimedItem> toDelete = claimedItemRepository.findExpiredClaimsForDeletion(deletionCutoff);

            if (!toDelete.isEmpty()) {
                log.info("Deleting {} old expired claims", toDelete.size());
                claimedItemRepository.deleteAll(toDelete);
            }

            log.info("=== EXPIRED CLAIMS CLEANUP COMPLETED ===");

        } catch (Exception e) {
            log.error("Error during expired claims cleanup", e);
        }
    }

    // PRIVATE HELPER METHODS

    private void validateClaimRequest(ClaimRequestDto request) {
        if (request.getLostItemId() == null || request.getLostItemId().trim().isEmpty()) {
            throw new IllegalArgumentException("Lost item ID is required");
        }
        if (request.getClaimerEmail() == null || request.getClaimerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Claimer email is required");
        }
        if (request.getClaimerName() == null || request.getClaimerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Claimer name is required");
        }
    }

    private  ItemDto fetchLostItem(String lostItemId) {
        try {
            // Add more logging to debug the issue
            log.info("Fetching lost item with ID: {}", lostItemId);

            // Make sure to call the correct endpoint
            ItemDto lostItem = itemServiceClient.getItemById(lostItemId);
            if (lostItem == null) {
                throw new RuntimeException("Lost item not found with ID: " + lostItemId);
            }

            log.info("Lost item retrieved successfully: {} (Type: {})", lostItem.getTitle(), lostItem.getItemType());

            // Ensure it's actually a lost item
            if (!"LOST".equalsIgnoreCase(lostItem.getItemType())) {
                throw new RuntimeException("Item with ID " + lostItemId + " is not a lost item");
            }

            return lostItem;
        } catch (FeignException e) {
            log.error("Feign exception when fetching lost item {}: Status={}, Message={}",
                    lostItemId, e.status(), e.getMessage());
            throw new RuntimeException("Could not fetch lost item from item service: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching lost item {}: {}", lostItemId, e.getMessage(), e);
            throw new RuntimeException("Unexpected error fetching lost item: " + e.getMessage());
        }
    }

    private void validateItemNotClaimed(String lostItemId) {
        Optional<ClaimedItem> existingClaim = claimedItemRepository
                .findByLostItemIdAndClaimStatus(lostItemId, ClaimStatus.ACTIVE);

        if (existingClaim.isPresent()) {
            throw new RuntimeException("This item has already been claimed");
        }
    }

    private List<ItemDto> findSimilarFoundItems(ItemDto lostItem) {
        try {
            // Get all found items (opposite of lost items)
            List<ItemDto> foundItems = itemServiceClient.getOppositeItems("LOST");

            // Filter by category first to improve performance
            List<ItemDto> categoryMatches = foundItems.stream()
                    .filter(item -> item.getCategory().equalsIgnoreCase(lostItem.getCategory()))
                    .collect(Collectors.toList());

            // If no category matches, use all found items
            List<ItemDto> itemsToCheck = categoryMatches.isEmpty() ? foundItems : categoryMatches;

            return itemsToCheck.stream()
                    .filter(foundItem -> {
                        try {
                            // Check image similarity
                            SimilarityResult similarity = imageTrackingServiceClient.checkSimilarity(
                                    lostItem.getImageUrl(), foundItem.getImageUrl());

                            boolean isMatch = similarity.getSimilarityScore() >= similarityThreshold;
                            log.debug("Similarity check - Lost: {}, Found: {}, Score: {}, Match: {}",
                                    lostItem.getTitle(), foundItem.getTitle(),
                                    similarity.getSimilarityScore(), isMatch);

                            return isMatch;
                        } catch (Exception e) {
                            log.warn("Error checking similarity for item: {}", foundItem.getId(), e);
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error finding similar items", e);
            return List.of();
        }
    }

    private ItemDto findBestMatch(ItemDto lostItem, List<ItemDto> matches) {
        ItemDto bestMatch = matches.get(0);
        double bestScore = 0.0;

        for (ItemDto match : matches) {
            try {
                SimilarityResult similarity = imageTrackingServiceClient.checkSimilarity(
                        lostItem.getImageUrl(), match.getImageUrl());

                if (similarity.getSimilarityScore() > bestScore) {
                    bestScore = similarity.getSimilarityScore();
                    bestMatch = match;
                }
            } catch (Exception e) {
                log.warn("Error comparing similarity for match: {}", match.getId(), e);
            }
        }

        log.info("Best match found: {} with similarity score: {}", bestMatch.getTitle(), bestScore);
        return bestMatch;
    }

    private ClaimedItem createClaimedItemRecord(ItemDto lostItem, ItemDto foundItem,
                                                ClaimRequestDto request, double similarity) {
        ClaimedItem claim = new ClaimedItem();

        // Set item information
        claim.setLostItemId(lostItem.getId());
        claim.setFoundItemId(foundItem.getId());
        claim.setLostItemTitle(lostItem.getTitle());
        claim.setFoundItemTitle(foundItem.getTitle());
        claim.setLostItemImageUrl(lostItem.getImageUrl());
        claim.setFoundItemImageUrl(foundItem.getImageUrl());
        claim.setLostItemUserId(lostItem.getUserId());
        claim.setFoundItemUserId(foundItem.getUserId());
        claim.setLostItemUserEmail(lostItem.getUserEmail());
        claim.setFoundItemUserEmail(foundItem.getUserEmail());

        // Set claim information
        claim.setClaimerName(request.getClaimerName());
        claim.setClaimerEmail(request.getClaimerEmail());
        claim.setClaimReason(request.getClaimReason());
        claim.setSimilarityScore(similarity);
        claim.setCategory(lostItem.getCategory());
        claim.setLocation(lostItem.getLocation());
        claim.setClaimStatus(ClaimStatus.ACTIVE);

        // Set timestamps (expiration based on configuration)
        claim.setClaimedAt(LocalDateTime.now());
        claim.setExpiresAt(LocalDateTime.now().plusDays(expirationDays));

        return claim;
    }

    private void deleteItemsFromMainSystem(String lostItemId, String foundItemId) {
        try {
            log.info("=== DELETING ITEMS FROM MAIN SYSTEM ===");
            log.info("Lost item ID to delete: {}", lostItemId);
            log.info("Found item ID to delete: {}", foundItemId);

            boolean lostDeleted = false;
            boolean foundDeleted = false;

            // Try to delete lost item
            try {
                ResponseEntity<String> lostResponse = itemServiceClient.deleteItem(lostItemId);
                log.info("Lost item deletion response: Status={}, Body={}",
                        lostResponse.getStatusCode(), lostResponse.getBody());
                lostDeleted = lostResponse.getStatusCode().is2xxSuccessful();
            } catch (FeignException e) {
                log.warn("Failed to delete lost item {}: Status={}, Message={}",
                        lostItemId, e.status(), e.getMessage());
                // Don't fail the entire process if one deletion fails
            } catch (Exception e) {
                log.warn("Unexpected error deleting lost item {}: {}", lostItemId, e.getMessage());
            }

            // Try to delete found item
            try {
                ResponseEntity<String> foundResponse = itemServiceClient.deleteItem(foundItemId);
                log.info("Found item deletion response: Status={}, Body={}",
                        foundResponse.getStatusCode(), foundResponse.getBody());
                foundDeleted = foundResponse.getStatusCode().is2xxSuccessful();
            } catch (FeignException e) {
                log.warn("Failed to delete found item {}: Status={}, Message={}",
                        foundItemId, e.status(), e.getMessage());
                // Don't fail the entire process if one deletion fails
            } catch (Exception e) {
                log.warn("Unexpected error deleting found item {}: {}", foundItemId, e.getMessage());
            }

            log.info("Deletion results - Lost: {}, Found: {}", lostDeleted, foundDeleted);

            // Only warn if both deletions failed
            if (!lostDeleted && !foundDeleted) {
                log.warn("Both item deletions failed, but claim will still be processed");
            }

            log.info("=== ITEM DELETION PROCESS COMPLETED ===");

        } catch (Exception e) {
            log.error("Unexpected error in deleteItemsFromMainSystem", e);
            // Don't fail the claim process due to deletion issues
            log.warn("Item deletion failed, but claim will still be processed");
        }
    }

    private void sendClaimNotifications(ItemDto lostItem, ItemDto foundItem,
                                        ClaimRequestDto request, double similarityScore) {
        try {
            // Notify the finder that someone claimed their found item
            if (!foundItem.getUserEmail().equals(request.getClaimerEmail())) {
                String finderMessage = String.format(
                        "Hello! Someone has claimed the '%s' you found. " +
                                "The claimer is %s (%s). Please coordinate with them to return the item. " +
                                "This claim will expire in %d days if not resolved.",
                        foundItem.getTitle(),
                        request.getClaimerName(),
                        request.getClaimerEmail(),
                        expirationDays
                );

                emailServiceClient.sendClaimRequestNotification(
                        Long.parseLong(foundItem.getUserId()),
                        foundItem.getUserEmail(),
                        foundItem.getTitle(),
                        finderMessage
                );
            }

            // Notify the claimer about successful claim
            String claimerMessage = String.format(
                    "Your claim for '%s' has been processed successfully! " +
                            "A matching found item '%s' was located with %.2f%% similarity. " +
                            "Please contact the finder at %s to arrange pickup. " +
                            "This claim will expire in %d days.",
                    lostItem.getTitle(),
                    foundItem.getTitle(),
                    similarityScore * 100,
                    foundItem.getUserEmail(),
                    expirationDays
            );

            emailServiceClient.sendItemMatchedNotification(
                    Long.parseLong(lostItem.getUserId()),
                    request.getClaimerEmail(),
                    lostItem.getTitle(),
                    claimerMessage
            );

        } catch (Exception e) {
            log.warn("Error sending claim notifications (claim still processed successfully)", e);
            // Don't fail the claim process if notification fails
        }
    }

    private ClaimedItemDto mapToDto(ClaimedItem entity) {
        ClaimedItemDto dto = new ClaimedItemDto();
        dto.setId(entity.getId());
        dto.setLostItemId(entity.getLostItemId());
        dto.setFoundItemId(entity.getFoundItemId());
        dto.setLostItemTitle(entity.getLostItemTitle());
        dto.setFoundItemTitle(entity.getFoundItemTitle());
        dto.setLostItemImageUrl(entity.getLostItemImageUrl());
        dto.setFoundItemImageUrl(entity.getFoundItemImageUrl());
        dto.setLostItemUserId(entity.getLostItemUserId());
        dto.setFoundItemUserId(entity.getFoundItemUserId());
        dto.setLostItemUserEmail(entity.getLostItemUserEmail());
        dto.setFoundItemUserEmail(entity.getFoundItemUserEmail());
        dto.setClaimerName(entity.getClaimerName());
        dto.setClaimerEmail(entity.getClaimerEmail());
        dto.setClaimReason(entity.getClaimReason());
        dto.setSimilarityScore(entity.getSimilarityScore());
        dto.setCategory(entity.getCategory());
        dto.setLocation(entity.getLocation());
        dto.setClaimedAt(entity.getClaimedAt());
        dto.setExpiresAt(entity.getExpiresAt());
        dto.setClaimStatus(entity.getClaimStatus());
        return dto;
    }

    // Inner class for statistics
    public static class ClaimStatistics {
        private final long activeClaims;
        private final long expiredClaims;
        private final long resolvedClaims;

        public ClaimStatistics(long activeClaims, long expiredClaims, long resolvedClaims) {
            this.activeClaims = activeClaims;
            this.expiredClaims = expiredClaims;
            this.resolvedClaims = resolvedClaims;
        }

        // Getters
        public long getActiveClaims() { return activeClaims; }
        public long getExpiredClaims() { return expiredClaims; }
        public long getResolvedClaims() { return resolvedClaims; }
        public long getTotalClaims() { return activeClaims + expiredClaims + resolvedClaims; }
    }

    public List<ClaimedItemDto> getAllActiveClaims() {
        try {
            List<ClaimedItem> claims = claimedItemRepository.findAll()
                    .stream()
                    .filter(claim -> claim.getClaimStatus() == ClaimStatus.ACTIVE)
                    .collect(Collectors.toList());

            return claims.stream().map(this::mapToDto).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all active claims", e);
            throw new RuntimeException("Failed to fetch active claims");
        }
    }
}