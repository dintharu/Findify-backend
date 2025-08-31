package edu.icet.ecom.dto;


import edu.icet.ecom.enums.ConversationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {

    private Long id;
    private Long user1Id;
    private Long user2Id;
    private Long lostItemId;
    private Long foundItemId;
    private String itemTitle;
    private ConversationStatus status;
    private Boolean isResolved;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private MessageDto lastMessage;
    private Long unreadCount;
    private UserDto otherUser; // The person you're chatting with
    private String otherUserName;
}
