package edu.icet.ecom.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRequestDto {

    private String lostItemId;
    private String foundItemId;
    private String claimerName;
    private String claimerEmail;
    private String claimReason;

    // Constructor for single item claim (when user clicks claim on their own lost item)
    public ClaimRequestDto(String lostItemId, String claimerName, String claimerEmail, String claimReason) {
        this.lostItemId = lostItemId;
        this.claimerName = claimerName;
        this.claimerEmail = claimerEmail;
        this.claimReason = claimReason;
    }
}