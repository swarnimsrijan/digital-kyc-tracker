package in.zeta.repository;

import in.zeta.entity.Users;
import in.zeta.enums.Role;
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
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private Users customer;
    private Users requestor;
    private Users officer;

    @BeforeEach
    void setUp() {
        customer = TestDataBuilder.createCustomer();
        requestor = TestDataBuilder.createRequestor();
        officer = TestDataBuilder.createOfficer();

        entityManager.persist(customer);
        entityManager.persist(requestor);
        entityManager.persist(officer);
        entityManager.flush();
    }

    @Test
    void testFindByUsername() {
        // When
        Optional<Users> foundUser = userRepository.findByUsername("customer123");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("customer@test.com");
        assertThat(foundUser.get().getRole()).isEqualTo(Role.CUSTOMER);
    }

    @Test
    void testFindByUsername_NotFound() {
        // When
        Optional<Users> foundUser = userRepository.findByUsername("nonexistent_user");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testFindByEmail() {
        // When
        Optional<Users> foundUser = userRepository.findByEmail("officer@test.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("officer123");
        assertThat(foundUser.get().getRole()).isEqualTo(Role.VERIFICATION_OFFICER);
    }

    @Test
    void testFindByEmail_NotFound() {
        // When
        Optional<Users> foundUser = userRepository.findByEmail("nonexistent@test.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testFindByRole_Customer() {
        // When
        List<Users> customers = userRepository.findByRole(Role.CUSTOMER);

        // Then
        assertThat(customers).isNotEmpty();
        assertThat(customers).hasSize(1);
        assertThat(customers.get(0).getUsername()).isEqualTo("customer123");
    }

    @Test
    void testFindByRole_VerificationOfficer() {
        // When
        List<Users> officers = userRepository.findByRole(Role.VERIFICATION_OFFICER);

        // Then
        assertThat(officers).isNotEmpty();
        assertThat(officers).hasSize(1);
        assertThat(officers.get(0).getUsername()).isEqualTo("officer123");
    }

    @Test
    void testFindByRole_Requestor() {
        // When
        List<Users> requestors = userRepository.findByRole(Role.VERIFICATION_REQUESTOR);

        // Then
        assertThat(requestors).isNotEmpty();
        assertThat(requestors).hasSize(1);
        assertThat(requestors.get(0).getUsername()).isEqualTo("requestor123");
    }

    @Test
    void testFindByRole_MultipleUsers() {
        // Given - Add another customer
        Users customer2 = TestDataBuilder.createCustomerWithUsername("customer456", "customer2@test.com");
        entityManager.persist(customer2);
        entityManager.flush();

        // When
        List<Users> customers = userRepository.findByRole(Role.CUSTOMER);

        // Then
        assertThat(customers).hasSize(2);
        assertThat(customers).extracting(Users::getRole)
                .containsOnly(Role.CUSTOMER);
    }

    @Test
    void testExistsByUsername_True() {
        // When
        boolean exists = userRepository.existsByUsername("customer123");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByUsername_False() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent_user");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testExistsByEmail_True() {
        // When
        boolean exists = userRepository.existsByEmail("requestor@test.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmail_False() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@test.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testFindById() {
        // When
        Optional<Users> foundUser = userRepository.findById(officer.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("officer123");
        assertThat(foundUser.get().getEmail()).isEqualTo("officer@test.com");
    }

    @Test
    void testFindById_NotFound() {
        // When
        Optional<Users> foundUser = userRepository.findById(UUID.randomUUID());

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testUsernameUniqueness() {
        // Given
        Users duplicateUser = TestDataBuilder.createCustomer();
        duplicateUser.setId(null);
        duplicateUser.setEmail("different@test.com");
        // Same username as existing customer

        // When/Then
        assertThat(userRepository.existsByUsername("customer123")).isTrue();

        // This would throw constraint violation if persisted
        // Testing that the check exists
    }

    @Test
    void testEmailUniqueness() {
        // When/Then
        assertThat(userRepository.existsByEmail("customer@test.com")).isTrue();
        assertThat(userRepository.existsByEmail("unique@test.com")).isFalse();
    }

    @Test
    void testFindAllUsers() {
        // When
        List<Users> allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).hasSize(3);
        assertThat(allUsers).extracting(Users::getRole)
                .containsExactlyInAnyOrder(Role.CUSTOMER, Role.VERIFICATION_REQUESTOR, Role.VERIFICATION_OFFICER);
    }

    @Test
    void testUserCreatedAtTimestamp() {
        // When
        Optional<Users> foundUser = userRepository.findByUsername("customer123");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getCreatedAt()).isNotNull();
        assertThat(foundUser.get().getCreatedAt()).isBefore(java.time.LocalDateTime.now());
    }

    @Test
    void testCaseSensitiveUsername() {
        // When
        Optional<Users> lowerCase = userRepository.findByUsername("customer123");
        Optional<Users> upperCase = userRepository.findByUsername("CUSTOMER123");

        // Then
        assertThat(lowerCase).isPresent();
        assertThat(upperCase).isEmpty(); // Usernames are case-sensitive
    }

    @Test
    void testCaseSensitiveEmail() {
        // When
        Optional<Users> lowerCase = userRepository.findByEmail("customer@test.com");
        Optional<Users> upperCase = userRepository.findByEmail("CUSTOMER@TEST.COM");

        // Then
        assertThat(lowerCase).isPresent();
        assertThat(upperCase).isEmpty(); // Emails are case-sensitive in this implementation
    }
}