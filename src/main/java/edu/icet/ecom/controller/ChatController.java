//package edu.icet.ecom.controller;
//
//import edu.icet.ecom.dto.ChatMessageRequest;
//import edu.icet.ecom.dto.ConversationDto;
//import edu.icet.ecom.dto.MessageDto;
//import edu.icet.ecom.dto.StartChatRequest;
//import edu.icet.ecom.security.JwtTokenUtil;
//import edu.icet.ecom.service.ChatService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/chat")
//@RequiredArgsConstructor
//@Slf4j
//@CrossOrigin
//public class ChatController {
//
//    private final ChatService chatService;
//    private final JwtTokenUtil jwtTokenUtil;
//
//    @PostMapping("/conversations/start")
//    public ResponseEntity<ConversationDto> startConversation(@Valid @RequestBody StartChatRequest request,
//                                                             HttpServletRequest httpRequest) {
//        try {
//            Long userId = getUserIdFromRequest(httpRequest);
//            log.info("Starting conversation request from user: {}", userId);
//
//            ConversationDto conversation = chatService.startConversation(userId, request);
//            return ResponseEntity.ok(conversation);
//
//        } catch (Exception e) {
//            log.error("Error starting conversation: {}", e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @GetMapping("/conversations")
//    public ResponseEntity<List<ConversationDto>> getUserConversations(HttpServletRequest httpRequest) {
//        try {
//            Long userId = getUserIdFromRequest(httpRequest);
//            log.debug("Fetching conversations for user: {}", userId);
//
//            List<ConversationDto> conversations = chatService.getUserConversations(userId);
//            return ResponseEntity.ok(conversations);
//
//        } catch (Exception e) {
//            log.error("Error fetching conversations: {}", e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @PostMapping("/messages/send")
//    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody ChatMessageRequest request,
//                                                  HttpServletRequest httpRequest) {
//        try {
//            Long userId = getUserIdFromRequest(httpRequest);
//            log.debug("Sending message from user: {}", userId);
//
//            MessageDto message = chatService.sendMessage(userId, request);
//            return ResponseEntity.ok(message);
//
//        } catch (Exception e) {
//            log.error("Error sending message: {}", e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @GetMapping("/conversations/{conversationId}/messages")
//    public ResponseEntity<List<MessageDto>> getConversationMessages(@PathVariable Long conversationId,
//                                                                    @RequestParam(defaultValue = "0") int page,
//                                                                    @RequestParam(defaultValue = "50") int size,
//                                                                    HttpServletRequest httpRequest) {
//        try {
//            Long userId = getUserIdFromRequest(httpRequest);
//            log.debug("Fetching messages for conversation: {} by user: {}", conversationId, userId);
//
//            List<MessageDto> messages = chatService.getConversationMessages(userId, conversationId, page, size);
//            return ResponseEntity.ok(messages);
//
//        } catch (Exception e) {
//            log.error("Error fetching messages: {}", e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @PostMapping("/conversations/{conversationId}/mark-read")
//    public ResponseEntity<Void> markMessagesAsRead(@PathVariable Long conversationId,
//                                                   HttpServletRequest httpRequest) {
//        try {
//            Long userId = getUserIdFromRequest(httpRequest);
//            log.debug("Marking messages as read for conversation: {} by user: {}", conversationId, userId);
//
//            chatService.markMessagesAsRead(userId, conversationId);
//            return ResponseEntity.ok().build();
//
//        } catch (Exception e) {
//            log.error("Error marking messages as read: {}", e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @GetMapping("/unread-count")
//    public ResponseEntity<Map<String, Long>> getUnreadMessageCount(HttpServletRequest httpRequest) {
//        try {
//            Long userId = getUserIdFromRequest(httpRequest);
//            Long count = chatService.getUnreadMessageCount(userId);
//
//            return ResponseEntity.ok(Map.of("unreadCount", count));
//
//        } catch (Exception e) {
//            log.error("Error getting unread count: {}", e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @PostMapping("/conversations/{conversationId}/resolve")
//    public ResponseEntity<Void> resolveConversation(@PathVariable Long conversationId,
//                                                    HttpServletRequest httpRequest) {
//        try {
//            Long userId = getUserIdFromRequest(httpRequest);
//            log.info("Resolving conversation: {} by user: {}", conversationId, userId);
//
//            chatService.resolveConversation(userId, conversationId);
//            return ResponseEntity.ok().build();
//
//        } catch (Exception e) {
//            log.error("Error resolving conversation: {}", e.getMessage());
//            return ResponseEntity.badRequest().build();
//        }
//    }
//
//    @GetMapping("/health")
//    public ResponseEntity<Map<String, String>> healthCheck() {
//        return ResponseEntity.ok(Map.of("status", "UP", "service", "chat-service"));
//    }
//
//    private Long getUserIdFromRequest(HttpServletRequest request) {
//        String authHeader = request.getHeader("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//            return jwtTokenUtil.getUserIdFromToken(token);
//        }
//        throw new RuntimeException("Invalid or missing authorization header");
//    }
//
//}
// Fixed ChatController.java - COMPLETE SOLUTION
package edu.icet.ecom.controller;

import edu.icet.ecom.client.UserServiceClient;
import edu.icet.ecom.dto.*;
import edu.icet.ecom.service.ChatService;
import edu.icet.ecom.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j

public class ChatController {

    private final ChatService chatService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserServiceClient userServiceClient;

    @PostMapping("/start")
    public ResponseEntity<?> startChat(
            @RequestBody StartChatRequest request,
            HttpServletRequest httpRequest) {

        try {
            log.info("=== CHAT START REQUEST ===");
            log.info("Request data: {}", request);

            // FIXED: Extract user data from JWT token properly
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                log.error("❌ No authentication token provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Authentication token required"));
            }

            // FIXED: Get current user information properly
            Long currentUserId;
            String currentUserEmail;

            try {
                // Extract user ID from JWT token
                currentUserId = jwtTokenUtil.getUserIdFromToken(token);
                log.info("🔍 Current user ID from token: {}", currentUserId);

                // CRITICAL FIX: Get user details from user service to get real email
                UserDto currentUser = userServiceClient.getUserById(currentUserId);
                if (currentUser == null) {
                    throw new RuntimeException("User not found in user service");
                }

                currentUserEmail = currentUser.getEmail();
                if (currentUserEmail == null || currentUserEmail.trim().isEmpty()) {
                    throw new RuntimeException("User email not found");
                }

                log.info("✅ Current user resolved: ID={}, Email={}", currentUserId, currentUserEmail);

            } catch (Exception e) {
                log.error("❌ Failed to resolve current user: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid authentication token: " + e.getMessage()));
            }

            // FIXED: Validate other user exists and get their email
            Long otherUserId = request.getOtherUserId().longValue();
            String otherUserEmail;

            try {
                UserDto otherUser = userServiceClient.getUserById(otherUserId);
                if (otherUser == null) {
                    throw new RuntimeException("Other user not found");
                }

                otherUserEmail = otherUser.getEmail();
                if (otherUserEmail == null || otherUserEmail.trim().isEmpty()) {
                    throw new RuntimeException("Other user email not found");
                }

                log.info("✅ Other user resolved: ID={}, Email={}", otherUserId, otherUserEmail);

            } catch (Exception e) {
                log.error("❌ Failed to resolve other user: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Cannot find user to chat with: " + e.getMessage()));
            }

            // CRITICAL: Prevent self-chat by comparing both ID and email
            if (currentUserId.equals(otherUserId) ||
                    currentUserEmail.equalsIgnoreCase(otherUserEmail)) {
                log.warn("⚠️ User {} attempting to chat with themselves", currentUserEmail);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "You cannot start a chat with yourself"));
            }

            // FIXED: Set proper user identification in request
            request.setCurrentUserEmail(currentUserEmail);
            request.setOtherUserEmail(otherUserEmail); // Add this to your StartChatRequest if not present

            log.info("✅ Starting chat conversation...");
            ConversationDto conversation = chatService.startConversation(request);

            log.info("✅ Chat started successfully: Conversation ID = {}", conversation.getId());
            return ResponseEntity.ok(conversation);

        } catch (IllegalArgumentException e) {
            log.error("❌ Validation error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("❌ Unexpected error starting chat: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start chat: " + e.getMessage()));
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getUserConversations(HttpServletRequest httpRequest) {
        try {
            // FIXED: Get user email properly
            String currentUserEmail = resolveUserEmailFromToken(httpRequest);
            log.info("📨 Fetching conversations for user: {}", currentUserEmail);

            List<ConversationDto> conversations = chatService.getUserConversations(currentUserEmail);

            log.info("✅ Found {} conversations for user {}", conversations.size(), currentUserEmail);
            return ResponseEntity.ok(conversations);

        } catch (Exception e) {
            log.error("❌ Error fetching conversations: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch conversations"));
        }
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long conversationId,
            @RequestBody ChatMessageRequest request,
            HttpServletRequest httpRequest) {

        try {
            // FIXED: Get user email properly
            String currentUserEmail = resolveUserEmailFromToken(httpRequest);
            request.setSenderEmail(currentUserEmail);

            log.info("💬 Sending message in conversation {}: From {}", conversationId, currentUserEmail);

            MessageDto message = chatService.sendMessage(conversationId, request);

            log.info("✅ Message sent successfully");
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("❌ Error sending message: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to send message"));
        }
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<?> getConversationMessages(
            @PathVariable Long conversationId,
            HttpServletRequest httpRequest) {

        try {
            // FIXED: Get user email properly
            String currentUserEmail = resolveUserEmailFromToken(httpRequest);
            log.info("📖 Fetching messages for conversation {}: User {}", conversationId, currentUserEmail);

            List<MessageDto> messages = chatService.getConversationMessages(conversationId, currentUserEmail);

            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            log.error("❌ Error fetching messages: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch messages"));
        }
    }

    // FIXED: Enhanced token extraction with multiple fallback strategies
    private String extractTokenFromRequest(HttpServletRequest request) {
        // Strategy 1: Standard Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            log.debug("🔑 Token extracted from Authorization header (length: {})", token.length());
            return token;
        }

        // Strategy 2: Direct token header (fallback)
        String directToken = request.getHeader("X-Auth-Token");
        if (directToken != null && !directToken.trim().isEmpty()) {
            log.debug("🔑 Token extracted from X-Auth-Token header");
            return directToken.trim();
        }

        // Strategy 3: Check for token in other common headers
        String tokenHeader = request.getHeader("Token");
        if (tokenHeader != null && !tokenHeader.trim().isEmpty()) {
            log.debug("🔑 Token extracted from Token header");
            return tokenHeader.trim();
        }

        log.warn("⚠️ No valid token found in request headers");
        log.debug("Available headers: {}", java.util.Collections.list(request.getHeaderNames()));
        return null;
    }

    // FIXED: Enhanced user email resolution with proper error handling
    private String resolveUserEmailFromToken(HttpServletRequest request) throws Exception {
        String token = extractTokenFromRequest(request);
        if (token == null) {
            throw new RuntimeException("No authentication token found");
        }

        return resolveUserEmailFromToken(token);
    }

    private String resolveUserEmailFromToken(String token) throws Exception {
        try {
            // Extract user ID from JWT token
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            String username = jwtTokenUtil.getUsernameFromToken(token);

            log.debug("🔍 Token contains: Username={}, UserID={}", username, userId);

            // Strategy 1: Check if username is already an email format
            if (username != null && username.contains("@") && username.contains(".")) {
                log.info("✅ Username is email format: {}", username);
                return username.toLowerCase().trim();
            }

            // Strategy 2: Get email from user service using the user ID
            log.debug("🔍 Fetching email from user service for ID: {}", userId);
            UserDto user = userServiceClient.getUserById(userId);

            if (user != null && user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                String email = user.getEmail().toLowerCase().trim();
                log.info("✅ Resolved email from user service: {}", email);
                return email;
            }

            throw new RuntimeException("Cannot resolve user email from token or user service");

        } catch (Exception e) {
            log.error("❌ Error resolving user email from token: {}", e.getMessage());
            throw new RuntimeException("Failed to resolve user email: " + e.getMessage());
        }
    }

    // FIXED: Health check endpoint for debugging
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "chat-service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    // FIXED: Test endpoint for debugging authentication
    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth(HttpServletRequest httpRequest) {
        try {
            String token = extractTokenFromRequest(httpRequest);
            if (token == null) {
                return ResponseEntity.ok(Map.of(
                        "status", "NO_TOKEN",
                        "message", "No authentication token found"
                ));
            }

            String userEmail = resolveUserEmailFromToken(token);
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            String username = jwtTokenUtil.getUsernameFromToken(token);

            return ResponseEntity.ok(Map.of(
                    "status", "AUTHENTICATED",
                    "userId", userId,
                    "username", username,
                    "email", userEmail,
                    "tokenLength", token.length()
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "ERROR",
                    "error", e.getMessage()
            ));
        }
    }
}