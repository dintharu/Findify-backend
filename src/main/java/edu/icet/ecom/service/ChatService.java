package edu.icet.ecom.service;

import edu.icet.ecom.dto.*;
import edu.icet.ecom.entity.Conversation;
import edu.icet.ecom.entity.Message;
import edu.icet.ecom.enums.ConversationStatus;
import edu.icet.ecom.enums.MessageType;
import edu.icet.ecom.repository.ConversationRepository;
import edu.icet.ecom.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserServiceClient userServiceClient;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ConversationDto startConversation(Long currentUserId, StartChatRequest request) {
        log.info("Starting conversation between user {} and user {} for items lost:{}, found:{}",
                currentUserId, request.getOtherUserId(), request.getLostItemId(), request.getFoundItemId());

        // Check if conversation already exists
        Optional<Conversation> existingConversation = conversationRepository.findExistingConversation(
                currentUserId, request.getOtherUserId(), request.getLostItemId(), request.getFoundItemId());

        if (existingConversation.isPresent()) {
            log.info("Found existing conversation: {}", existingConversation.get().getId());
            return mapToConversationDto(existingConversation.get(), currentUserId);
        }

        // Create new conversation
        Conversation conversation = new Conversation();
        conversation.setUser1Id(currentUserId);
        conversation.setUser2Id(request.getOtherUserId());
        conversation.setLostItemId(request.getLostItemId());
        conversation.setFoundItemId(request.getFoundItemId());
        conversation.setItemTitle(request.getItemTitle() != null ? request.getItemTitle() : "Item Match");
        conversation.setStatus(ConversationStatus.ACTIVE);
        conversation.setIsResolved(false);

        conversation = conversationRepository.save(conversation);
        log.info("Created new conversation with ID: {}", conversation.getId());

        // Send initial message
        if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
            ChatMessageRequest messageRequest = new ChatMessageRequest();
            messageRequest.setConversationId(conversation.getId());
            messageRequest.setReceiverId(request.getOtherUserId());
            messageRequest.setContent(request.getInitialMessage());
            messageRequest.setMessageType(MessageType.TEXT);

            sendMessage(currentUserId, messageRequest);
        }

        return mapToConversationDto(conversation, currentUserId);
    }

    public List<ConversationDto> getUserConversations(Long userId) {
        log.debug("Fetching conversations for user: {}", userId);
        List<Conversation> conversations = conversationRepository.findActiveConversationsByUserId(userId);

        return conversations.stream()
                .map(conversation -> mapToConversationDto(conversation, userId))
                .collect(Collectors.toList());
    }

    public List<MessageDto> getConversationMessages(Long userId, Long conversationId, int page, int size) {
        log.debug("Fetching messages for conversation: {} by user: {}", conversationId, userId);

        // Verify user has access to this conversation
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new RuntimeException("User not authorized to access this conversation");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Message> messages = messageRepository.findMessagesByConversationId(conversationId, pageable);

        return messages.stream()
                .map(this::mapToMessageDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDto sendMessage(Long senderId, ChatMessageRequest request) {
        log.debug("Sending message from user {} in conversation {}", senderId, request.getConversationId());

        // Verify conversation exists and user is participant
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Conversation not found with ID: " + request.getConversationId()));

        if (!conversation.getUser1Id().equals(senderId) && !conversation.getUser2Id().equals(senderId)) {
            throw new RuntimeException("User not authorized to send messages in this conversation");
        }

        // Create and save message
        Message message = new Message();
        message.setConversation(conversation);
        message.setSenderId(senderId);
        message.setReceiverId(request.getReceiverId());
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT);
        message.setAttachmentUrl(request.getAttachmentUrl());
        message.setAttachmentName(request.getAttachmentName());
        message.setIsRead(false);

        message = messageRepository.save(message);

        // Update conversation timestamp
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageDto messageDto = mapToMessageDto(message);

        // Send real-time notification via WebSocket
        try {
            messagingTemplate.convertAndSendToUser(
                    request.getReceiverId().toString(),
                    "/queue/messages",
                    messageDto
            );

            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversation.getId(),
                    messageDto
            );
        } catch (Exception e) {
            log.warn("Failed to send WebSocket notification: {}", e.getMessage());
        }

        return messageDto;
    }

    @Transactional
    public void markMessagesAsRead(Long userId, Long conversationId) {
        log.debug("Marking messages as read for conversation: {} by user: {}", conversationId, userId);

        // Verify user has access to this conversation
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new RuntimeException("User not authorized to access this conversation");
        }

        messageRepository.markMessagesAsRead(conversationId, userId, LocalDateTime.now());

        // Send WebSocket notification about read status
        try {
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversationId + "/read",
                    userId
            );
        } catch (Exception e) {
            log.warn("Failed to send read status notification: {}", e.getMessage());
        }
    }

    public Long getUnreadMessageCount(Long userId) {
        return conversationRepository.countUnreadMessagesByUserId(userId);
    }

    @Transactional
    public void resolveConversation(Long userId, Long conversationId) {
        log.info("Resolving conversation: {} by user: {}", conversationId, userId);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
            throw new RuntimeException("User not authorized to resolve this conversation");
        }

        conversation.setIsResolved(true);
        conversation.setStatus(ConversationStatus.RESOLVED);
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        log.info("Conversation {} resolved successfully", conversationId);
    }

    private ConversationDto mapToConversationDto(Conversation conversation, Long currentUserId) {
        ConversationDto dto = new ConversationDto();
        dto.setId(conversation.getId());
        dto.setUser1Id(conversation.getUser1Id());
        dto.setUser2Id(conversation.getUser2Id());
        dto.setLostItemId(conversation.getLostItemId());
        dto.setFoundItemId(conversation.getFoundItemId());
        dto.setItemTitle(conversation.getItemTitle());
        dto.setStatus(conversation.getStatus());
        dto.setIsResolved(conversation.getIsResolved());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setUpdatedAt(conversation.getUpdatedAt());

        // Get other user info
        Long otherUserId = conversation.getUser1Id().equals(currentUserId) ?
                conversation.getUser2Id() : conversation.getUser1Id();

        try {
            UserDto otherUser = userServiceClient.getUserById(otherUserId);
            dto.setOtherUser(otherUser);
            dto.setOtherUserName(otherUser != null ?
                    (otherUser.getFirstName() + " " + otherUser.getLastName()).trim() : "User " + otherUserId);
        } catch (Exception e) {
            log.warn("Could not fetch other user info: {}", e.getMessage());
            dto.setOtherUserName("User " + otherUserId);
        }

        // Get last message
        Optional<Message> lastMessage = messageRepository.findLatestMessageByConversationId(conversation.getId());
        if (lastMessage.isPresent()) {
            dto.setLastMessage(mapToMessageDto(lastMessage.get()));
        }

        // Get unread count for current user
        dto.setUnreadCount(messageRepository.countUnreadMessagesByConversationAndUser(
                conversation.getId(), currentUserId));

        return dto;
    }

    private MessageDto mapToMessageDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSenderId());
        dto.setReceiverId(message.getReceiverId());
        dto.setContent(message.getContent());
        dto.setMessageType(message.getMessageType());
        dto.setIsRead(message.getIsRead());
        dto.setSentAt(message.getSentAt());
        dto.setReadAt(message.getReadAt());
        dto.setAttachmentUrl(message.getAttachmentUrl());
        dto.setAttachmentName(message.getAttachmentName());

        // Add sender name for display
        try {
            String senderName = userServiceClient.getUserDisplayName(message.getSenderId());
            dto.setSenderName(senderName);
        } catch (Exception e) {
            dto.setSenderName("User " + message.getSenderId());
        }

        return dto;
    }
}