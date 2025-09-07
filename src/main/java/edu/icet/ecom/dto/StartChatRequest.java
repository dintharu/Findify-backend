//package edu.icet.ecom.dto;
//
//
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.NotNull;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class StartChatRequest {
//    @NotNull(message = "Other user ID is required")
//    private Long otherUserId;
//
//    @NotNull(message = "Lost item ID is required")
//    private Long lostItemId;
//
//    @NotNull(message = "Found item ID is required")
//    private Long foundItemId;
//
//    private String itemTitle;
//
//    @NotBlank(message = "Initial message is required")
//    private String initialMessage;
//}


package edu.icet.ecom.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartChatRequest {

    @NotNull(message = "Other user ID is required")
    private Integer otherUserId;

    @NotNull(message = "Lost item ID is required")
    private Integer lostItemId;

    @NotNull(message = "Found item ID is required")
    private Integer foundItemId;

    @NotBlank(message = "Item title is required")
    private String itemTitle;

    @NotBlank(message = "Initial message is required")
    private String initialMessage;

    // CRITICAL ADDITION: Current user email for enhanced user identification
    @Email(message = "Current user email must be valid")
    private String currentUserEmail;

    // Optional: Other user email for validation
    private String otherUserEmail;



    // Helper method to validate the request
    public boolean isValid() {
        return otherUserId != null && otherUserId > 0 &&
                lostItemId != null && lostItemId > 0 &&
                foundItemId != null && foundItemId > 0 &&
                itemTitle != null && !itemTitle.trim().isEmpty() &&
                initialMessage != null && !initialMessage.trim().isEmpty() &&
                currentUserEmail != null && !currentUserEmail.trim().isEmpty();
    }

    // Helper method for debugging
    @Override
    public String toString() {
        return String.format("StartChatRequest{" +
                        "otherUserId=%d, " +
                        "lostItemId=%d, " +
                        "foundItemId=%d, " +
                        "itemTitle='%s', " +
                        "currentUserEmail='%s', " +
                        "otherUserEmail='%s', " +
                        "messageLength=%d}",
                otherUserId, lostItemId, foundItemId, itemTitle,
                currentUserEmail, otherUserEmail,
                initialMessage != null ? initialMessage.length() : 0);
    }
}