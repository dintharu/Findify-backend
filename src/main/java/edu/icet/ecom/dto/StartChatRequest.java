package edu.icet.ecom.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StartChatRequest {
    @NotNull(message = "Other user ID is required")
    private Long otherUserId;

    @NotNull(message = "Lost item ID is required")
    private Long lostItemId;

    @NotNull(message = "Found item ID is required")
    private Long foundItemId;

    private String itemTitle;

    @NotBlank(message = "Initial message is required")
    private String initialMessage;
}
