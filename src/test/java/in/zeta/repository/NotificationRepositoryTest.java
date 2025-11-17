package in.zeta.repository;
import in.zeta.dto.response.NotificationResponse;
import in.zeta.entity.Notification;
import in.zeta.entity.Users;
import in.zeta.entity.VerificationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private Users customer;
    private Users requestor;
    private Users officer;
    private VerificationRequest verificationRequest;

    @BeforeEach
    void setUp() {
        customer = TestDataBuilder.createCustomer();
        requestor = TestDataBuilder.createRequestor();
        officer = TestDataBuilder.createOfficer();

        entityManager.persist(customer);
        entityManager.persist(requestor);
        entityManager.persist(officer);

        verificationRequest = TestDataBuilder.createVerificationRequest(customer, requestor, officer);
        entityManager.persist(verificationRequest);
        entityManager.flush();
    }

    @Test
    void testFindByUserIdOrderByCreatedAtDesc() {
        // Given
        Notification notification1 = TestDataBuilder.createNotification(customer, verificationRequest);
        Notification notification2 = TestDataBuilder.createNotification(customer, verificationRequest);
        notification2.setMessage("Second notification");

        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.flush();

        // When
        List<NotificationResponse> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(customer.getId());

        // Then
        assertThat(notifications).isNotEmpty();
        assertThat(notifications).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testFindByUserIdAndIsReadFalseOrderByCreatedAtDesc() {
        // Given
        Notification unreadNotification = TestDataBuilder.createNotification(customer, verificationRequest);
        Notification readNotification = TestDataBuilder.createReadNotification(customer, verificationRequest);

        entityManager.persist(unreadNotification);
        entityManager.persist(readNotification);
        entityManager.flush();

        // When
        List<NotificationResponse> unreadNotifications =
                notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(customer.getId());

        // Then
        assertThat(unreadNotifications).isNotEmpty();
        assertThat(unreadNotifications).allMatch(n -> !n.isRead());
    }

    @Test
    void testCountByUserIdAndIsReadFalse() {
        // Given
        Notification unread1 = TestDataBuilder.createNotification(customer, verificationRequest);
        Notification unread2 = TestDataBuilder.createNotification(customer, verificationRequest);
        Notification read = TestDataBuilder.createReadNotification(customer, verificationRequest);

        entityManager.persist(unread1);
        entityManager.persist(unread2);
        entityManager.persist(read);
        entityManager.flush();

        // When
        Long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(customer.getId());

        // Then
        assertThat(unreadCount).isEqualTo(2L);
    }

    @Test
    void testFindById() {
        // Given
        Notification notification = TestDataBuilder.createNotification(customer, verificationRequest);
        entityManager.persist(notification);
        entityManager.flush();

        // When
        Optional<Notification> foundNotification = notificationRepository.findById(notification.getId());

        // Then
        assertThat(foundNotification).isPresent();
        assertThat(foundNotification.get().getMessage()).isEqualTo(notification.getMessage());
    }

    @Test
    void testFindUnreadNotificationsByUserId() {
        // Given
        Notification unread1 = TestDataBuilder.createNotification(customer, verificationRequest);
        Notification unread2 = TestDataBuilder.createNotification(customer, verificationRequest);
        Notification read = TestDataBuilder.createReadNotification(customer, verificationRequest);

        entityManager.persist(unread1);
        entityManager.persist(unread2);
        entityManager.persist(read);
        entityManager.flush();

        // When
        List<Notification> unreadNotifications =
                notificationRepository.findUnreadNotificationsByUserId(customer.getId());

        // Then
        assertThat(unreadNotifications).hasSize(2);
        assertThat(unreadNotifications).allMatch(n -> !n.getIsRead());
    }

    @Test
    void testFindByNonExistentUserId() {
        // When
        List<NotificationResponse> notifications =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(UUID.randomUUID());

        // Then
        assertThat(notifications).isEmpty();
    }

    @Test
    void testCountByUserIdAndIsReadFalse_NoUnreadNotifications() {
        // Given - only read notifications
        Notification read = TestDataBuilder.createReadNotification(customer, verificationRequest);
        entityManager.persist(read);
        entityManager.flush();

        // When
        Long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(customer.getId());

        // Then
        assertThat(unreadCount).isEqualTo(0L);
    }
}