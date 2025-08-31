package edu.icet.ecom.model.dto;

import edu.icet.ecom.enums.ClaimStatus;
import edu.icet.ecom.model.entity.ClaimedItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimedItemDto {

    private Long id;
    private String lostItemId;
    private String foundItemId;
    private String lostItemTitle;
    private String foundItemTitle;
    private String lostItemImageUrl;
    private String foundItemImageUrl;
    private String lostItemUserId;
    private String foundItemUserId;
    private String lostItemUserEmail;
    private String foundItemUserEmail;
    private String claimerName;
    private String claimerEmail;
    private String claimReason;
    private Double similarityScore;
    private String category;
    private String location;
    private LocalDateTime claimedAt;
    private LocalDateTime expiresAt;
    private ClaimStatus claimStatus;

    // Helper method to check if claim is expired
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Helper method to get days remaining
    public long getDaysRemaining() {
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toDays();
    }
}