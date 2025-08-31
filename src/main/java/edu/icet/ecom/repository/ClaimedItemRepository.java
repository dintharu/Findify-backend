package edu.icet.ecom.repository;

import edu.icet.ecom.enums.ClaimStatus;
import edu.icet.ecom.model.entity.ClaimedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimedItemRepository extends JpaRepository<ClaimedItem, Long> {

    // Find all active claims for a specific user (either as owner or finder)
    @Query("SELECT c FROM ClaimedItem c WHERE " +
            "(c.lostItemUserEmail = :userEmail OR c.foundItemUserEmail = :userEmail) " +
            "AND c.claimStatus = 'ACTIVE' " +
            "ORDER BY c.claimedAt DESC")
    List<ClaimedItem> findActiveClaimsByUserEmail(@Param("userEmail") String userEmail);

    // Find all expired claims that need to be cleaned up
    @Query("SELECT c FROM ClaimedItem c WHERE c.expiresAt < :now AND c.claimStatus = 'ACTIVE'")
    List<ClaimedItem> findExpiredClaims(@Param("now") LocalDateTime now);

    // Find claims by lost item ID
    Optional<ClaimedItem> findByLostItemIdAndClaimStatus(String lostItemId, ClaimStatus status);

    // Find claims by found item ID
    Optional<ClaimedItem> findByFoundItemIdAndClaimStatus(String foundItemId, ClaimStatus status);

    // Check if either lost or found item is already claimed
    @Query("SELECT c FROM ClaimedItem c WHERE " +
            "(c.lostItemId = :itemId OR c.foundItemId = :itemId) " +
            "AND c.claimStatus = 'ACTIVE'")
    List<ClaimedItem> findActiveClaimsByItemId(@Param("itemId") String itemId);

    // Find all claims for a specific user as claimer
    List<ClaimedItem> findByClaimerEmailAndClaimStatusOrderByClaimedAtDesc(
            String claimerEmail, ClaimStatus status);

    // Find expired claims for cleanup
    @Query("SELECT c FROM ClaimedItem c WHERE c.claimStatus = 'EXPIRED' AND c.expiresAt < :cutoffDate")
    List<ClaimedItem> findExpiredClaimsForDeletion(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Count active claims for statistics
    long countByClaimStatus(ClaimStatus status);
}