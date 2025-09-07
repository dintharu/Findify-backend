//package edu.icet.ecom.service;
//
//import edu.icet.ecom.dto.*;
//import edu.icet.ecom.entity.Conversation;
//import edu.icet.ecom.entity.Message;
//import edu.icet.ecom.enums.ConversationStatus;
//import edu.icet.ecom.enums.MessageType;
//import edu.icet.ecom.repository.ConversationRepository;
//import edu.icet.ecom.repository.MessageRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ChatService {
//
//    private final ConversationRepository conversationRepository;
//    private final MessageRepository messageRepository;
//    private final UserServiceClient userServiceClient;
//    private final SimpMessagingTemplate messagingTemplate;
//
//    @Transactional
//    public ConversationDto startConversation(Long currentUserId, StartChatRequest request) {
//        log.info("Starting conversation between user {} and user {} for items lost:{}, found:{}",
//                currentUserId, request.getOtherUserId(), request.getLostItemId(), request.getFoundItemId());
//
//        // Check if conversation already exists
//        Optional<Conversation> existingConversation = conversationRepository.findExistingConversation(
//                currentUserId, request.getOtherUserId(), request.getLostItemId(), request.getFoundItemId());
//
//        if (existingConversation.isPresent()) {
//            log.info("Found existing conversation: {}", existingConversation.get().getId());
//            return mapToConversationDto(existingConversation.get(), currentUserId);
//        }
//
//        // Create new conversation
//        Conversation conversation = new Conversation();
//        conversation.setUser1Id(currentUserId);
//        conversation.setUser2Id(request.getOtherUserId());
//        conversation.setLostItemId(request.getLostItemId());
//        conversation.setFoundItemId(request.getFoundItemId());
//        conversation.setItemTitle(request.getItemTitle() != null ? request.getItemTitle() : "Item Match");
//        conversation.setStatus(ConversationStatus.ACTIVE);
//        conversation.setIsResolved(false);
//
//        conversation = conversationRepository.save(conversation);
//        log.info("Created new conversation with ID: {}", conversation.getId());
//
//        // Send initial message
//        if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
//            ChatMessageRequest messageRequest = new ChatMessageRequest();
//            messageRequest.setConversationId(conversation.getId());
//            messageRequest.setReceiverId(request.getOtherUserId());
//            messageRequest.setContent(request.getInitialMessage());
//            messageRequest.setMessageType(MessageType.TEXT);
//
//            sendMessage(currentUserId, messageRequest);
//        }
//
//        return mapToConversationDto(conversation, currentUserId);
//    }
//
//    public List<ConversationDto> getUserConversations(Long userId) {
//        log.debug("Fetching conversations for user: {}", userId);
//        List<Conversation> conversations = conversationRepository.findActiveConversationsByUserId(userId);
//
//        return conversations.stream()
//                .map(conversation -> mapToConversationDto(conversation, userId))
//                .collect(Collectors.toList());
//    }
//
//    public List<MessageDto> getConversationMessages(Long userId, Long conversationId, int page, int size) {
//        log.debug("Fetching messages for conversation: {} by user: {}", conversationId, userId);
//
//        // Verify user has access to this conversation
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
//            throw new RuntimeException("User not authorized to access this conversation");
//        }
//
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Message> messages = messageRepository.findMessagesByConversationId(conversationId, pageable);
//
//        return messages.stream()
//                .map(this::mapToMessageDto)
//                .collect(Collectors.toList());
//    }
//
//    @Transactional
//    public MessageDto sendMessage(Long senderId, ChatMessageRequest request) {
//        log.debug("Sending message from user {} in conversation {}", senderId, request.getConversationId());
//
//        // Verify conversation exists and user is participant
//        Conversation conversation = conversationRepository.findById(request.getConversationId())
//                .orElseThrow(() -> new RuntimeException("Conversation not found with ID: " + request.getConversationId()));
//
//        if (!conversation.getUser1Id().equals(senderId) && !conversation.getUser2Id().equals(senderId)) {
//            throw new RuntimeException("User not authorized to send messages in this conversation");
//        }
//
//        // Create and save message
//        Message message = new Message();
//        message.setConversation(conversation);
//        message.setSenderId(senderId);
//        message.setReceiverId(request.getReceiverId());
//        message.setContent(request.getContent());
//        message.setMessageType(request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT);
//        message.setAttachmentUrl(request.getAttachmentUrl());
//        message.setAttachmentName(request.getAttachmentName());
//        message.setIsRead(false);
//
//        message = messageRepository.save(message);
//
//        // Update conversation timestamp
//        conversation.setUpdatedAt(LocalDateTime.now());
//        conversationRepository.save(conversation);
//
//        MessageDto messageDto = mapToMessageDto(message);
//
//        // Send real-time notification via WebSocket
//        try {
//            messagingTemplate.convertAndSendToUser(
//                    request.getReceiverId().toString(),
//                    "/queue/messages",
//                    messageDto
//            );
//
//            messagingTemplate.convertAndSend(
//                    "/topic/conversation/" + conversation.getId(),
//                    messageDto
//            );
//        } catch (Exception e) {
//            log.warn("Failed to send WebSocket notification: {}", e.getMessage());
//        }
//
//        return messageDto;
//    }
//
//    @Transactional
//    public void markMessagesAsRead(Long userId, Long conversationId) {
//        log.debug("Marking messages as read for conversation: {} by user: {}", conversationId, userId);
//
//        // Verify user has access to this conversation
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
//            throw new RuntimeException("User not authorized to access this conversation");
//        }
//
//        messageRepository.markMessagesAsRead(conversationId, userId, LocalDateTime.now());
//
//        // Send WebSocket notification about read status
//        try {
//            messagingTemplate.convertAndSend(
//                    "/topic/conversation/" + conversationId + "/read",
//                    userId
//            );
//        } catch (Exception e) {
//            log.warn("Failed to send read status notification: {}", e.getMessage());
//        }
//    }
//
//    public Long getUnreadMessageCount(Long userId) {
//        return conversationRepository.countUnreadMessagesByUserId(userId);
//    }
//
//    @Transactional
//    public void resolveConversation(Long userId, Long conversationId) {
//        log.info("Resolving conversation: {} by user: {}", conversationId, userId);
//
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
//            throw new RuntimeException("User not authorized to resolve this conversation");
//        }
//
//        conversation.setIsResolved(true);
//        conversation.setStatus(ConversationStatus.RESOLVED);
//        conversation.setUpdatedAt(LocalDateTime.now());
//        conversationRepository.save(conversation);
//
//        log.info("Conversation {} resolved successfully", conversationId);
//    }
//
//    private ConversationDto mapToConversationDto(Conversation conversation, Long currentUserId) {
//        ConversationDto dto = new ConversationDto();
//        dto.setId(conversation.getId());
//        dto.setUser1Id(conversation.getUser1Id());
//        dto.setUser2Id(conversation.getUser2Id());
//        dto.setLostItemId(conversation.getLostItemId());
//        dto.setFoundItemId(conversation.getFoundItemId());
//        dto.setItemTitle(conversation.getItemTitle());
//        dto.setStatus(conversation.getStatus());
//        dto.setIsResolved(conversation.getIsResolved());
//        dto.setCreatedAt(conversation.getCreatedAt());
//        dto.setUpdatedAt(conversation.getUpdatedAt());
//
//        // Get other user info
//        Long otherUserId = conversation.getUser1Id().equals(currentUserId) ?
//                conversation.getUser2Id() : conversation.getUser1Id();
//
//        try {
//            UserDto otherUser = userServiceClient.getUserById(otherUserId);
//            dto.setOtherUser(otherUser);
//            dto.setOtherUserName(otherUser != null ?
//                    (otherUser.getFirstName() + " " + otherUser.getLastName()).trim() : "User " + otherUserId);
//        } catch (Exception e) {
//            log.warn("Could not fetch other user info: {}", e.getMessage());
//            dto.setOtherUserName("User " + otherUserId);
//        }
//
//        // Get last message
//        Optional<Message> lastMessage = messageRepository.findLatestMessageByConversationId(conversation.getId());
//        if (lastMessage.isPresent()) {
//            dto.setLastMessage(mapToMessageDto(lastMessage.get()));
//        }
//
//        // Get unread count for current user
//        dto.setUnreadCount(messageRepository.countUnreadMessagesByConversationAndUser(
//                conversation.getId(), currentUserId));
//
//        return dto;
//    }
//
//    private MessageDto mapToMessageDto(Message message) {
//        MessageDto dto = new MessageDto();
//        dto.setId(message.getId());
//        dto.setConversationId(message.getConversation().getId());
//        dto.setSenderId(message.getSenderId());
//        dto.setReceiverId(message.getReceiverId());
//        dto.setContent(message.getContent());
//        dto.setMessageType(message.getMessageType());
//        dto.setIsRead(message.getIsRead());
//        dto.setSentAt(message.getSentAt());
//        dto.setReadAt(message.getReadAt());
//        dto.setAttachmentUrl(message.getAttachmentUrl());
//        dto.setAttachmentName(message.getAttachmentName());
//
//        // Add sender name for display
//        try {
//            String senderName = userServiceClient.getUserDisplayName(message.getSenderId());
//            dto.setSenderName(senderName);
//        } catch (Exception e) {
//            dto.setSenderName("User " + message.getSenderId());
//        }
//
//        return dto;
//    }
//}
//
//// Fixed ChatService.java - ENHANCED USER HANDLING
//package edu.icet.ecom.service;
//
//import edu.icet.ecom.dto.*;
//import edu.icet.ecom.entity.Conversation;
//import edu.icet.ecom.entity.Message;
//import edu.icet.ecom.enums.ConversationStatus;
//import edu.icet.ecom.enums.MessageType;
//import edu.icet.ecom.repository.ConversationRepository;
//import edu.icet.ecom.repository.MessageRepository;
//import edu.icet.ecom.client.UserServiceClient; // Import the correct client
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ChatService {
//
//    private final ConversationRepository conversationRepository;
//    private final MessageRepository messageRepository;
//    private final UserServiceClient userServiceClient;
//    private final SimpMessagingTemplate messagingTemplate;
//
//    // Cache for user email to ID mapping
//    private final Map<String, Long> emailToIdCache = new HashMap<>();
//
//    @Transactional
//    public ConversationDto startConversation(StartChatRequest request) {
//        try {
//            log.info("Starting conversation request: {}", request);
//
//            // FIXED: Resolve user IDs from emails with caching
//            Long currentUserId = getUserIdByEmail(request.getCurrentUserEmail());
//            Long otherUserId = request.getOtherUserId().longValue();
//
//            // Validate both users exist
//            if (currentUserId == null) {
//                throw new IllegalArgumentException("Current user not found with email: " + request.getCurrentUserEmail());
//            }
//
//            // Double-check other user exists
//            try {
//                UserDto otherUser = userServiceClient.getUserById(otherUserId);
//                if (otherUser == null) {
//                    throw new IllegalArgumentException("Other user not found with ID: " + otherUserId);
//                }
//            } catch (Exception e) {
//                throw new IllegalArgumentException("Cannot verify other user: " + e.getMessage());
//            }
//
//            log.info("✅ Resolved users - Current: {} ({}), Other: {} ({})",
//                    request.getCurrentUserEmail(), currentUserId,
//                    request.getOtherUserEmail(), otherUserId);
//
//            // Check if conversation already exists
//            Optional<Conversation> existingConversation = conversationRepository.findExistingConversation(
//                    currentUserId, otherUserId,
//                    request.getLostItemId().longValue(),
//                    request.getFoundItemId().longValue()
//            );
//
//            if (existingConversation.isPresent()) {
//                log.info("Found existing conversation: {}", existingConversation.get().getId());
//                return mapToConversationDto(existingConversation.get(), currentUserId);
//            }
//
//            // Create new conversation
//            Conversation conversation = new Conversation();
//            conversation.setUser1Id(currentUserId);
//            conversation.setUser2Id(otherUserId);
//            conversation.setLostItemId(request.getLostItemId().longValue());
//            conversation.setFoundItemId(request.getFoundItemId().longValue());
//            conversation.setItemTitle(request.getItemTitle());
//            conversation.setStatus(ConversationStatus.ACTIVE);
//            conversation.setIsResolved(false);
//
//            conversation = conversationRepository.save(conversation);
//            log.info("✅ Created new conversation with ID: {}", conversation.getId());
//
//            // Send initial message
//            if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
//                ChatMessageRequest messageRequest = new ChatMessageRequest();
//                messageRequest.setConversationId(conversation.getId());
//                messageRequest.setReceiverId(otherUserId);
//                messageRequest.setContent(request.getInitialMessage());
//                messageRequest.setMessageType(MessageType.TEXT);
//
//                sendMessage(conversation.getId(), messageRequest);
//            }
//
//            return mapToConversationDto(conversation, currentUserId);
//
//        } catch (Exception e) {
//            log.error("❌ Error starting conversation: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to start conversation: " + e.getMessage());
//        }
//    }
//
//    // FIXED: Get conversations by user email instead of ID
//    public List<ConversationDto> getUserConversations(String userEmail) {
//        try {
//            Long userId = getUserIdByEmail(userEmail);
//            if (userId == null) {
//                throw new IllegalArgumentException("User not found with email: " + userEmail);
//            }
//
//            log.debug("Fetching conversations for user: {} (ID: {})", userEmail, userId);
//            List<Conversation> conversations = conversationRepository.findActiveConversationsByUserId(userId);
//
//            return conversations.stream()
//                    .map(conversation -> mapToConversationDto(conversation, userId))
//                    .collect(Collectors.toList());
//
//        } catch (Exception e) {
//            log.error("❌ Error fetching conversations for {}: {}", userEmail, e.getMessage(), e);
//            throw new RuntimeException("Failed to fetch conversations: " + e.getMessage());
//        }
//    }
//
//    // FIXED: Enhanced message sending with email-based user identification
//    @Transactional
//    public MessageDto sendMessage(Long conversationId, ChatMessageRequest request) {
//        try {
//            log.debug("Sending message in conversation {} from {}", conversationId, request.getSenderEmail());
//
//            // Resolve sender ID from email
//            Long senderId = getUserIdByEmail(request.getSenderEmail());
//            if (senderId == null) {
//                throw new IllegalArgumentException("Sender not found with email: " + request.getSenderEmail());
//            }
//
//            // Verify conversation exists and user is participant
//            Conversation conversation = conversationRepository.findById(conversationId)
//                    .orElseThrow(() -> new RuntimeException("Conversation not found with ID: " + conversationId));
//
//            if (!conversation.getUser1Id().equals(senderId) && !conversation.getUser2Id().equals(senderId)) {
//                throw new RuntimeException("User not authorized to send messages in this conversation");
//            }
//
//            // Create and save message
//            Message message = new Message();
//            message.setConversation(conversation);
//            message.setSenderId(senderId);
//            message.setReceiverId(request.getReceiverId());
//            message.setContent(request.getContent());
//            message.setMessageType(request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT);
//            message.setAttachmentUrl(request.getAttachmentUrl());
//            message.setAttachmentName(request.getAttachmentName());
//            message.setIsRead(false);
//
//            message = messageRepository.save(message);
//
//            // Update conversation timestamp
//            conversation.setUpdatedAt(LocalDateTime.now());
//            conversationRepository.save(conversation);
//
//            MessageDto messageDto = mapToMessageDto(message);
//
//            // Send real-time notification via WebSocket
//            sendWebSocketNotifications(conversation, messageDto);
//
//            return messageDto;
//
//        } catch (Exception e) {
//            log.error("❌ Error sending message: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to send message: " + e.getMessage());
//        }
//    }
//
//    // FIXED: Get conversation messages by email
//    public List<MessageDto> getConversationMessages(Long conversationId, String userEmail) {
//        try {
//            Long userId = getUserIdByEmail(userEmail);
//            if (userId == null) {
//                throw new IllegalArgumentException("User not found with email: " + userEmail);
//            }
//
//            // Verify user has access to this conversation
//            Conversation conversation = conversationRepository.findById(conversationId)
//                    .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//            if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
//                throw new RuntimeException("User not authorized to access this conversation");
//            }
//
//            List<Message> messages = messageRepository.findAllMessagesByConversationId(conversationId);
//            return messages.stream()
//                    .map(this::mapToMessageDto)
//                    .collect(Collectors.toList());
//
//        } catch (Exception e) {
//            log.error("❌ Error fetching messages: {}", e.getMessage(), e);
//            throw new RuntimeException("Failed to fetch messages: " + e.getMessage());
//        }
//    }
//
//    // ENHANCED: Helper method to get user ID by email with caching
//    private Long getUserIdByEmail(String email) {
//        if (email == null || email.trim().isEmpty()) {
//            return null;
//        }
//
//        String cleanEmail = email.toLowerCase().trim();
//
//        // Check cache first
//        if (emailToIdCache.containsKey(cleanEmail)) {
//            return emailToIdCache.get(cleanEmail);
//        }
//
//        try {
//            // Try to get user by email from user service
//            UserDto user = userServiceClient.getUserByEmail(cleanEmail);
//            if (user != null && user.getId() != null) {
//                // Cache the result
//                emailToIdCache.put(cleanEmail, user.getId());
//                log.debug("✅ Resolved user {} -> ID {}", cleanEmail, user.getId());
//                return user.getId();
//            }
//
//            log.warn("⚠️ User not found for email: {}", cleanEmail);
//            return null;
//
//        } catch (Exception e) {
//            log.error("❌ Error resolving user ID for email {}: {}", cleanEmail, e.getMessage());
//            return null;
//        }
//    }
//
//    // ENHANCED: Send WebSocket notifications
//    private void sendWebSocketNotifications(Conversation conversation, MessageDto messageDto) {
//        try {
//            // Send to receiver's personal queue
//            messagingTemplate.convertAndSendToUser(
//                    messageDto.getReceiverId().toString(),
//                    "/queue/messages",
//                    messageDto
//            );
//
//            // Send to conversation topic
//            messagingTemplate.convertAndSend(
//                    "/topic/conversation/" + conversation.getId(),
//                    messageDto
//            );
//
//            log.debug("✅ WebSocket notifications sent for message {}", messageDto.getId());
//
//        } catch (Exception e) {
//            log.warn("⚠️ Failed to send WebSocket notification: {}", e.getMessage());
//        }
//    }
//
//    // ENHANCED: Map conversation to DTO with better user handling
//    private ConversationDto mapToConversationDto(Conversation conversation, Long currentUserId) {
//        ConversationDto dto = new ConversationDto();
//        dto.setId(conversation.getId());
//        dto.setUser1Id(conversation.getUser1Id());
//        dto.setUser2Id(conversation.getUser2Id());
//        dto.setLostItemId(conversation.getLostItemId());
//        dto.setFoundItemId(conversation.getFoundItemId());
//        dto.setItemTitle(conversation.getItemTitle());
//        dto.setStatus(conversation.getStatus());
//        dto.setIsResolved(conversation.getIsResolved());
//        dto.setCreatedAt(conversation.getCreatedAt());
//        dto.setUpdatedAt(conversation.getUpdatedAt());
//
//        // Get other user info
//        Long otherUserId = conversation.getUser1Id().equals(currentUserId) ?
//                conversation.getUser2Id() : conversation.getUser1Id();
//
//        try {
//            UserDto otherUser = userServiceClient.getUserById(otherUserId);
//            dto.setOtherUser(otherUser);
//
//            if (otherUser != null) {
//                // Try to build full name or use available name fields
//                String displayName = buildDisplayName(otherUser);
//                dto.setOtherUserName(displayName);
//            } else {
//                dto.setOtherUserName("User " + otherUserId);
//            }
//
//        } catch (Exception e) {
//            log.warn("Could not fetch other user info for ID {}: {}", otherUserId, e.getMessage());
//            dto.setOtherUserName("User " + otherUserId);
//        }
//
//        // Get last message
//        Optional<Message> lastMessage = messageRepository.findLatestMessageByConversationId(conversation.getId());
//        if (lastMessage.isPresent()) {
//            dto.setLastMessage(mapToMessageDto(lastMessage.get()));
//        }
//
//        // Get unread count for current user
//        dto.setUnreadCount(messageRepository.countUnreadMessagesByConversationAndUser(
//                conversation.getId(), currentUserId));
//
//        return dto;
//    }
//
//    // Helper to build display name from UserDto
//    private String buildDisplayName(UserDto user) {
//        if (user.getFirstName() != null && user.getLastName() != null) {
//            return (user.getFirstName() + " " + user.getLastName()).trim();
//        } else if (user.getFirstName() != null) {
//            return user.getFirstName();
//        } else if (user.getLastName() != null) {
//            return user.getLastName();
//        } else if (user.getUsername() != null) {
//            return user.getUsername();
//        } else {
//            return "User " + user.getId();
//        }
//    }
//
//    // ENHANCED: Map message to DTO with better user handling
//    private MessageDto mapToMessageDto(Message message) {
//        MessageDto dto = new MessageDto();
//        dto.setId(message.getId());
//        dto.setConversationId(message.getConversation().getId());
//        dto.setSenderId(message.getSenderId());
//        dto.setReceiverId(message.getReceiverId());
//        dto.setContent(message.getContent());
//        dto.setMessageType(message.getMessageType());
//        dto.setIsRead(message.getIsRead());
//        dto.setSentAt(message.getSentAt());
//        dto.setReadAt(message.getReadAt());
//        dto.setAttachmentUrl(message.getAttachmentUrl());
//        dto.setAttachmentName(message.getAttachmentName());
//
//        // Add sender name for display
//        try {
//            UserDto sender = userServiceClient.getUserById(message.getSenderId());
//            if (sender != null) {
//                dto.setSenderName(buildDisplayName(sender));
//            } else {
//                dto.setSenderName("User " + message.getSenderId());
//            }
//        } catch (Exception e) {
//            dto.setSenderName("User " + message.getSenderId());
//        }
//
//        return dto;
//    }
//
//    // Additional methods (markMessagesAsRead, resolveConversation, etc.) remain the same...
//    @Transactional
//    public void markMessagesAsRead(Long userId, Long conversationId) {
//        messageRepository.markMessagesAsRead(conversationId, userId, LocalDateTime.now());
//    }
//
//    public Long getUnreadMessageCount(Long userId) {
//        return conversationRepository.countUnreadMessagesByUserId(userId);
//    }
//
//    @Transactional
//    public void resolveConversation(Long userId, Long conversationId) {
//        Conversation conversation = conversationRepository.findById(conversationId)
//                .orElseThrow(() -> new RuntimeException("Conversation not found"));
//
//        conversation.setIsResolved(true);
//        conversation.setStatus(ConversationStatus.RESOLVED);
//        conversationRepository.save(conversation);
//    }
//}


package edu.icet.ecom.service;

import edu.icet.ecom.dto.*;
import edu.icet.ecom.entity.Conversation;
import edu.icet.ecom.entity.Message;
import edu.icet.ecom.enums.ConversationStatus;
import edu.icet.ecom.enums.MessageType;
import edu.icet.ecom.repository.ConversationRepository;
import edu.icet.ecom.repository.MessageRepository;
import edu.icet.ecom.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    // Cache for user email to ID mapping
    private final Map<String, Long> emailToIdCache = new HashMap<>();
    private final Map<Long, UserDto> userCache = new HashMap<>();

    @Transactional
    public ConversationDto startConversation(StartChatRequest request) {
        try {
            log.info("Starting conversation request: {}", request);

            // FIXED: Resolve user IDs from emails with proper validation
            Long currentUserId = getUserIdByEmail(request.getCurrentUserEmail());
            Long otherUserId = request.getOtherUserId().longValue();

            // Validate both users exist
            if (currentUserId == null) {
                throw new IllegalArgumentException("Current user not found with email: " + request.getCurrentUserEmail());
            }

            // Verify other user exists in user service
            UserDto otherUser = fetchAndCacheUser(otherUserId);
            if (otherUser == null) {
                throw new IllegalArgumentException("Other user not found with ID: " + otherUserId);
            }

            log.info("✅ Resolved users - Current: {} ({}), Other: {} ({})",
                    request.getCurrentUserEmail(), currentUserId,
                    otherUser.getEmail(), otherUserId);

            // Prevent self-chat
            if (currentUserId.equals(otherUserId)) {
                throw new IllegalArgumentException("Cannot start a chat with yourself");
            }

            // Check if conversation already exists
            Optional<Conversation> existingConversation = conversationRepository.findExistingConversation(
                    currentUserId, otherUserId,
                    request.getLostItemId().longValue(),
                    request.getFoundItemId().longValue()
            );

            if (existingConversation.isPresent()) {
                log.info("Found existing conversation: {}", existingConversation.get().getId());
                return mapToConversationDto(existingConversation.get(), currentUserId);
            }

            // Create new conversation
            Conversation conversation = new Conversation();
            conversation.setUser1Id(currentUserId);
            conversation.setUser2Id(otherUserId);
            conversation.setLostItemId(request.getLostItemId().longValue());
            conversation.setFoundItemId(request.getFoundItemId().longValue());
            conversation.setItemTitle(request.getItemTitle());
            conversation.setStatus(ConversationStatus.ACTIVE);
            conversation.setIsResolved(false);

            conversation = conversationRepository.save(conversation);
            log.info("✅ Created new conversation with ID: {}", conversation.getId());

            // Send initial message
            if (request.getInitialMessage() != null && !request.getInitialMessage().trim().isEmpty()) {
                ChatMessageRequest messageRequest = new ChatMessageRequest();
                messageRequest.setConversationId(conversation.getId());
                messageRequest.setReceiverId(otherUserId);
                messageRequest.setContent(request.getInitialMessage());
                messageRequest.setMessageType(MessageType.TEXT);
                messageRequest.setSenderEmail(request.getCurrentUserEmail());

                sendMessage(conversation.getId(), messageRequest);
            }

            return mapToConversationDto(conversation, currentUserId);

        } catch (Exception e) {
            log.error("❌ Error starting conversation: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to start conversation: " + e.getMessage());
        }
    }

    // FIXED: Get conversations by user email with better error handling
    public List<ConversationDto> getUserConversations(String userEmail) {
        try {
            Long userId = getUserIdByEmail(userEmail);
            if (userId == null) {
                throw new IllegalArgumentException("User not found with email: " + userEmail);
            }

            log.debug("Fetching conversations for user: {} (ID: {})", userEmail, userId);
            List<Conversation> conversations = conversationRepository.findActiveConversationsByUserId(userId);

            return conversations.stream()
                    .map(conversation -> mapToConversationDto(conversation, userId))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error fetching conversations for {}: {}", userEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch conversations: " + e.getMessage());
        }
    }

    // FIXED: Enhanced message sending
    @Transactional
    public MessageDto sendMessage(Long conversationId, ChatMessageRequest request) {
        try {
            log.debug("Sending message in conversation {} from {}", conversationId, request.getSenderEmail());

            // Resolve sender ID from email
            Long senderId = getUserIdByEmail(request.getSenderEmail());
            if (senderId == null) {
                throw new IllegalArgumentException("Sender not found with email: " + request.getSenderEmail());
            }

            // Verify conversation exists and user is participant
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found with ID: " + conversationId));

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
            sendWebSocketNotifications(conversation, messageDto);

            return messageDto;

        } catch (Exception e) {
            log.error("❌ Error sending message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    // FIXED: Get conversation messages by email
    public List<MessageDto> getConversationMessages(Long conversationId, String userEmail) {
        try {
            Long userId = getUserIdByEmail(userEmail);
            if (userId == null) {
                throw new IllegalArgumentException("User not found with email: " + userEmail);
            }

            // Verify user has access to this conversation
            Conversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation not found"));

            if (!conversation.getUser1Id().equals(userId) && !conversation.getUser2Id().equals(userId)) {
                throw new RuntimeException("User not authorized to access this conversation");
            }

            List<Message> messages = messageRepository.findAllMessagesByConversationId(conversationId);
            return messages.stream()
                    .map(this::mapToMessageDto)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error fetching messages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch messages: " + e.getMessage());
        }
    }

    // ENHANCED: Helper method to get user ID by email with better caching
    private Long getUserIdByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String cleanEmail = email.toLowerCase().trim();

        // Check cache first
        if (emailToIdCache.containsKey(cleanEmail)) {
            return emailToIdCache.get(cleanEmail);
        }

        try {
            // Try to get user by email from user service
            UserDto user = userServiceClient.getUserByEmail(cleanEmail);
            if (user != null && user.getId() != null) {
                // Cache both email->ID and ID->user
                emailToIdCache.put(cleanEmail, user.getId());
                userCache.put(user.getId(), user);

                log.debug("✅ Resolved user {} -> ID {}", cleanEmail, user.getId());
                return user.getId();
            }

            log.warn("⚠️ User not found for email: {}", cleanEmail);
            return null;

        } catch (Exception e) {
            log.error("❌ Error resolving user ID for email {}: {}", cleanEmail, e.getMessage());
            return null;
        }
    }

    // ENHANCED: Fetch and cache user data
    private UserDto fetchAndCacheUser(Long userId) {
        if (userId == null) {
            return null;
        }

        // Check cache first
        if (userCache.containsKey(userId)) {
            return userCache.get(userId);
        }

        try {
            UserDto user = userServiceClient.getUserById(userId);
            if (user != null) {
                // Cache the user data
                userCache.put(userId, user);

                // Also cache email mapping if available
                if (user.getEmail() != null) {
                    emailToIdCache.put(user.getEmail().toLowerCase().trim(), userId);
                }

                log.debug("✅ Fetched and cached user: ID {} -> {}", userId, user.getEmail());
            }
            return user;
        } catch (Exception e) {
            log.error("❌ Error fetching user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    // ENHANCED: Send WebSocket notifications
    private void sendWebSocketNotifications(Conversation conversation, MessageDto messageDto) {
        try {
            // Send to receiver's personal queue
            messagingTemplate.convertAndSendToUser(
                    messageDto.getReceiverId().toString(),
                    "/queue/messages",
                    messageDto
            );

            // Send to conversation topic
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversation.getId(),
                    messageDto
            );

            log.debug("✅ WebSocket notifications sent for message {}", messageDto.getId());

        } catch (Exception e) {
            log.warn("⚠️ Failed to send WebSocket notification: {}", e.getMessage());
        }
    }

    // ENHANCED: Map conversation to DTO with better user handling
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
            UserDto otherUser = fetchAndCacheUser(otherUserId);
            dto.setOtherUser(otherUser);

            if (otherUser != null) {
                String displayName = buildDisplayName(otherUser);
                dto.setOtherUserName(displayName);
            } else {
                dto.setOtherUserName("User " + otherUserId);
            }

        } catch (Exception e) {
            log.warn("Could not fetch other user info for ID {}: {}", otherUserId, e.getMessage());
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

    // FIXED: Build display name from UserDto matching your user service response
    private String buildDisplayName(UserDto user) {
        if (user == null) {
            return "Unknown User";
        }

        // Try fullName first (matches your user service response)
        if (user.getFullName() != null && !user.getFullName().trim().isEmpty()) {
            return user.getFullName().trim();
        }

        // Fallback to firstName + lastName
        if (user.getFirstName() != null || user.getLastName() != null) {
            String firstName = user.getFirstName() != null ? user.getFirstName().trim() : "";
            String lastName = user.getLastName() != null ? user.getLastName().trim() : "";
            String fullName = (firstName + " " + lastName).trim();
            if (!fullName.isEmpty()) {
                return fullName;
            }
        }

        // Fallback to username
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            return user.getUsername().trim();
        }

        // Fallback to email username
        if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
            String emailUsername = user.getEmail().split("@")[0];
            return emailUsername.substring(0, 1).toUpperCase() + emailUsername.substring(1);
        }

        return "User " + user.getId();
    }

    // ENHANCED: Map message to DTO with better user handling
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
            UserDto sender = fetchAndCacheUser(message.getSenderId());
            if (sender != null) {
                dto.setSenderName(buildDisplayName(sender));
            } else {
                dto.setSenderName("User " + message.getSenderId());
            }
        } catch (Exception e) {
            dto.setSenderName("User " + message.getSenderId());
        }

        return dto;
    }

    // Additional methods for marking as read, etc.
    @Transactional
    public void markMessagesAsRead(Long userId, Long conversationId) {
        messageRepository.markMessagesAsRead(conversationId, userId, LocalDateTime.now());
    }

    public Long getUnreadMessageCount(Long userId) {
        return conversationRepository.countUnreadMessagesByUserId(userId);
    }

    @Transactional
    public void resolveConversation(Long userId, Long conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        conversation.setIsResolved(true);
        conversation.setStatus(ConversationStatus.RESOLVED);
        conversationRepository.save(conversation);
    }
}