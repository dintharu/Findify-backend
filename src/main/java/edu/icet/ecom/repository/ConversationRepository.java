package edu.icet.ecom.repository;


import edu.icet.ecom.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation,Long> {
    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.user1Id = :userId OR c.user2Id = :userId) AND c.status = 'ACTIVE' " +
            "ORDER BY c.updatedAt DESC")
    List<Conversation> findActiveConversationsByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c WHERE " +
            "((c.user1Id = :user1Id AND c.user2Id = :user2Id) OR " +
            "(c.user1Id = :user2Id AND c.user2Id = :user1Id)) AND " +
            "c.lostItemId = :lostItemId AND c.foundItemId = :foundItemId AND " +
            "c.status = 'ACTIVE'")
    Optional<Conversation> findExistingConversation(@Param("user1Id") Long user1Id,
                                                    @Param("user2Id") Long user2Id,
                                                    @Param("lostItemId") Long lostItemId,
                                                    @Param("foundItemId") Long foundItemId);

    @Query("SELECT COUNT(m) FROM Message m JOIN m.conversation c WHERE " +
            "((c.user1Id = :userId AND m.senderId = c.user2Id) OR " +
            "(c.user2Id = :userId AND m.senderId = c.user1Id)) AND " +
            "m.isRead = false AND c.status = 'ACTIVE'")
    Long countUnreadMessagesByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c WHERE " +
            "(c.lostItemId = :itemId OR c.foundItemId = :itemId) AND " +
            "c.status = 'ACTIVE'")
    List<Conversation> findConversationsByItemId(@Param("itemId") Long itemId);
}
