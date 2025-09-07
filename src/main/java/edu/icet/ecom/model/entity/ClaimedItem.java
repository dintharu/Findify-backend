package edu.icet.ecom.model.entity;

import edu.icet.ecom.enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "claimed_items", indexes = {
        @Index(name = "idx_lost_item_status", columnList = "lost_item_id, claim_status"),
        @Index(name = "idx_found_item_status", columnList = "found_item_id, claim_status"),
        @Index(name = "idx_claimer_email", columnList = "claimer_email"),
        @Index(name = "idx_expires_at", columnList = "expires_at"),
        @Index(name = "idx_claim_status", columnList = "claim_status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lost_item_id", nullable = false, length = 50)
    private String lostItemId;

    @Column(name = "found_item_id", nullable = false, length = 50)
    private String foundItemId;

    @Column(name = "lost_item_title", length = 500)
    private String lostItemTitle;

    @Column(name = "found_item_title", length = 500)
    private String foundItemTitle;

    // FIXED: Use LONGTEXT for image URLs to handle large base64 data
    @Lob
    @Column(name = "lost_item_image_url", columnDefinition = "LONGTEXT")
    private String lostItemImageUrl;

    @Lob
    @Column(name = "found_item_image_url", columnDefinition = "LONGTEXT")
    private String foundItemImageUrl;

    @Column(name = "lost_item_user_id", length = 50)
    private String lostItemUserId;

    @Column(name = "found_item_user_id", length = 50)
    private String foundItemUserId;

    @Column(name = "lost_item_user_email", length = 100)
    private String lostItemUserEmail;

    @Column(name = "found_item_user_email", length = 100)
    private String foundItemUserEmail;

    @Column(name = "claimer_name", nullable = false, length = 100)
    private String claimerName;

    @Column(name = "claimer_email", nullable = false, length = 100)
    private String claimerEmail;

    @Lob
    @Column(name = "claim_reason", columnDefinition = "TEXT")
    private String claimReason;

    @Column(name = "similarity_score")
    private Double similarityScore;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "claim_status", nullable = false, length = 20)
    private ClaimStatus claimStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (claimedAt == null) {
            claimedAt = now;
        }
        if (expiresAt == null) {
            expiresAt = claimedAt.plusDays(30);
        }
        if (claimStatus == null) {
            claimStatus = ClaimStatus.ACTIVE;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}