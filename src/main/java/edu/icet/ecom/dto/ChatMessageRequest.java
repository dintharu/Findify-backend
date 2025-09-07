package edu.icet.ecom.dto;

import edu.icet.ecom.enums.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequest {


    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotBlank(message = "Message content cannot be empty")
    private String content;

    private MessageType messageType = MessageType.TEXT;
    private String attachmentUrl;
    private String attachmentName;
    private String senderEmail;

}
