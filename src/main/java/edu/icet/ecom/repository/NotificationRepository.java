package edu.icet.ecom.repository;

import edu.icet.ecom.enums.NotificationStatus;
import edu.icet.ecom.model.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity,Long> {

    List<NotificationEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<NotificationEntity> findByStatusOrderByCreatedAtDesc(NotificationStatus status);

    @Query("SELECT n FROM NotificationEntity n WHERE n.status = 'FAILED' AND n.createdAt > :retryTime")
    List<NotificationEntity> findFailedNotificationsForRetry(LocalDateTime retryTime);

    Long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime dateTime);

}
