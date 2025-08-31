package edu.icet.ecom.controller;

import edu.icet.ecom.dto.ChatMessageRequest;
import edu.icet.ecom.dto.ConversationDto;
import edu.icet.ecom.dto.MessageDto;
import edu.icet.ecom.dto.StartChatRequest;
import edu.icet.ecom.security.JwtTokenUtil;
import edu.icet.ecom.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin
public class ChatController {

    private final ChatService chatService;
    private final JwtTokenUtil jwtTokenUtil;

    @PostMapping("/conversations/start")
    public ResponseEntity<ConversationDto> startConversation(@Valid @RequestBody StartChatRequest request,
                                                             HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            log.info("Starting conversation request from user: {}", userId);

            ConversationDto conversation = chatService.startConversation(userId, request);
            return ResponseEntity.ok(conversation);

        } catch (Exception e) {
            log.error("Error starting conversation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDto>> getUserConversations(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            log.debug("Fetching conversations for user: {}", userId);

            List<ConversationDto> conversations = chatService.getUserConversations(userId);
            return ResponseEntity.ok(conversations);

        } catch (Exception e) {
            log.error("Error fetching conversations: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/messages/send")
    public ResponseEntity<MessageDto> sendMessage(@Valid @RequestBody ChatMessageRequest request,
                                                  HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            log.debug("Sending message from user: {}", userId);

            MessageDto message = chatService.sendMessage(userId, request);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageDto>> getConversationMessages(@PathVariable Long conversationId,
                                                                    @RequestParam(defaultValue = "0") int page,
                                                                    @RequestParam(defaultValue = "50") int size,
                                                                    HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            log.debug("Fetching messages for conversation: {} by user: {}", conversationId, userId);

            List<MessageDto> messages = chatService.getConversationMessages(userId, conversationId, page, size);
            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            log.error("Error fetching messages: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/conversations/{conversationId}/mark-read")
    public ResponseEntity<Void> markMessagesAsRead(@PathVariable Long conversationId,
                                                   HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            log.debug("Marking messages as read for conversation: {} by user: {}", conversationId, userId);

            chatService.markMessagesAsRead(userId, conversationId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error marking messages as read: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadMessageCount(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            Long count = chatService.getUnreadMessageCount(userId);

            return ResponseEntity.ok(Map.of("unreadCount", count));

        } catch (Exception e) {
            log.error("Error getting unread count: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/conversations/{conversationId}/resolve")
    public ResponseEntity<Void> resolveConversation(@PathVariable Long conversationId,
                                                    HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromRequest(httpRequest);
            log.info("Resolving conversation: {} by user: {}", conversationId, userId);

            chatService.resolveConversation(userId, conversationId);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error resolving conversation: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "chat-service"));
    }

    private Long getUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtTokenUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("Invalid or missing authorization header");
    }

}
