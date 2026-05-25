package com.example.notification_service.repository;

import com.example.notification_service.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Notification> findByStatusOrderByCreatedAtDesc(Notification.NotificationStatus status);
    List<Notification> findTop50ByStatusAndNextRetryAtLessThanEqualAndRetryCountLessThanOrderByCreatedAtAsc(
            Notification.NotificationStatus status,
            LocalDateTime nextRetryAt,
            Integer retryCount);
}
