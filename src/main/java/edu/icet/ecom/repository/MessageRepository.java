package edu.icet.ecom.repository;

import edu.icet.ecom.entity.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.sentAt DESC")
    Page<Message> findMessagesByConversationId(@Param("conversationId") Long conversationId,
                                               Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.sentAt ASC")
    List<Message> findAllMessagesByConversationId(@Param("conversationId") Long conversationId);

    @Query("SELECT m FROM Message m WHERE m.conversation.id = :conversationId " +
            "ORDER BY m.sentAt DESC LIMIT 1")
    Optional<Message> findLatestMessageByConversationId(@Param("conversationId") Long conversationId);

    @Query("SELECT COUNT(m) FROM Message m WHERE " +
            "m.conversation.id = :conversationId AND " +
            "m.receiverId = :userId AND m.isRead = false")
    Long countUnreadMessagesByConversationAndUser(@Param("conversationId") Long conversationId,
                                                  @Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = :readAt WHERE " +
            "m.conversation.id = :conversationId AND m.receiverId = :userId AND m.isRead = false")
    void markMessagesAsRead(@Param("conversationId") Long conversationId,
                            @Param("userId") Long userId,
                            @Param("readAt") LocalDateTime readAt);

    @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.isRead = false " +
            "ORDER BY m.sentAt DESC")
    List<Message> findUnreadMessagesByUserId(@Param("userId") Long userId);
}