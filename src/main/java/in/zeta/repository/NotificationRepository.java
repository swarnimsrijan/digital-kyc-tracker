package in.zeta.repository;

import in.zeta.entity.Notification;
import in.zeta.dto.response.NotificationResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


//public interface NotificationRepository extends JpaRepository<Notification, UUID> {
//    List<NotificationResponse> findByUserIdOrderByCreatedAtDesc(UUID userID);
//    List<NotificationResponse> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(UUID userId);
//    Long countByUserIdAndIsReadFalse(UUID userId);
//    Optional<Notification> findById(UUID notificationId);
//    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.id DESC")
//    List<Notification> findUnreadNotificationsByUserId(@Param("userId") UUID userId);
//}
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("SELECT new in.zeta.dto.response.NotificationResponse(n.id, n.user.id, n.verificationRequest.id, n.notificationType, n.message, n.createdAt, n.isRead, n.readAt) " +
            "FROM Notification n WHERE n.user.id = :userId ORDER BY n.createdAt DESC")
    List<NotificationResponse> findByUserIdOrderByCreatedAtDesc(@Param("userId") UUID userId);

    @Query("SELECT new in.zeta.dto.response.NotificationResponse(n.id, n.user.id, n.verificationRequest.id, n.notificationType, n.message, n.createdAt, n.isRead, n.readAt) " +
            "FROM Notification n WHERE n.user.id = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<NotificationResponse> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(@Param("userId") UUID userId);

    Long countByUserIdAndIsReadFalse(UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = false")
    List<Notification> findUnreadNotificationsByUserId(@Param("userId") UUID userId);
}