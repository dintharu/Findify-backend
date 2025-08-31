package edu.icet.ecom.controller;


import edu.icet.ecom.dto.ChatMessageRequest;
import edu.icet.ecom.dto.MessageDto;
import edu.icet.ecom.security.JwtTokenUtil;
import edu.icet.ecom.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketController {
    private final ChatService chatService;
    private final JwtTokenUtil jwtTokenUtil;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request,
                            SimpMessageHeaderAccessor headerAccessor,
                            Principal principal) {
        try {
            // Get user ID from JWT token in header
            String token = headerAccessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Long userId = jwtTokenUtil.getUserIdFromToken(token);

                log.debug("WebSocket message from user: {} to conversation: {}", userId, request.getConversationId());

                MessageDto message = chatService.sendMessage(userId, request);
                log.info("WebSocket message sent successfully: {}", message.getId());

            } else {
                log.warn("No valid JWT token found in WebSocket message");
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage(), e);
        }
    }

    @MessageMapping("/chat.markRead")
    public void markAsRead(@Payload Long conversationId,
                           SimpMessageHeaderAccessor headerAccessor,
                           Principal principal) {
        try {
            String token = headerAccessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                Long userId = jwtTokenUtil.getUserIdFromToken(token);

                log.debug("WebSocket mark as read from user: {} for conversation: {}", userId, conversationId);

                chatService.markMessagesAsRead(userId, conversationId);
                log.info("Messages marked as read via WebSocket for conversation: {}", conversationId);

            } else {
                log.warn("No valid JWT token found in WebSocket mark read request");
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket mark as read: {}", e.getMessage(), e);
        }
    }
}
