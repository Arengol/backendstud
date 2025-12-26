
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.astondevs.learn.vorobev.dao.HibernateUtil;
import ru.astondevs.learn.vorobev.dao.UserDAO;
import ru.astondevs.learn.vorobev.dao.UserDAOHibernateImpl;
import ru.astondevs.learn.vorobev.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@DisplayName("Интеграционные тесты для UserDAOHibernateImpl")
@ExtendWith({org.testcontainers.junit.jupiter.TestcontainersExtension.class})
class UserDAOHibernateImplIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private UserDAO userDAO;
    private User testUser;

    @BeforeEach
    void setUp() {
        HibernateUtil.shutdown();
        HibernateUtil.init(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        userDAO = new UserDAOHibernateImpl();
        cleanupDatabase();
        testUser = User.builder()
                .name("Тестовый Пользователь")
                .email("test@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private void cleanupDatabase() {
        try (var session = HibernateUtil.getSessionFactory().openSession()) {
            var transaction = session.beginTransaction();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            transaction.commit();
        }
    }

    @Test
    @DisplayName("Сохранение пользователя - успешно")
    void save_ShouldSaveUserAndReturnId() {
        // Act
        Long userId = userDAO.save(testUser);

        // Assert
        assertThat(userId).isNotNull();
        Optional<User> savedUser = userDAO.findById(userId);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("Тестовый Пользователь");
        assertThat(savedUser.get().getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.get().getAge()).isEqualTo(30);
    }

    @Test
    @DisplayName("Поиск пользователя по ID - успешно")
    void findById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        Long userId = userDAO.save(testUser);

        // Act
        Optional<User> foundUser = userDAO.findById(userId);

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Поиск пользователя по ID - возвращает пустой Optional")
    void findById_ShouldReturnEmptyOptional_WhenUserNotExists() {
        // Act
        Optional<User> foundUser = userDAO.findById(999L);

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Получение всех пользователей - успешно")
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        User user1 = User.builder()
                .name("Пользователь 1")
                .email("user1@example.com")
                .age(25)
                .build();

        User user2 = User.builder()
                .name("Пользователь 2")
                .email("user2@example.com")
                .age(35)
                .build();

        userDAO.save(user1);
        userDAO.save(user2);

        // Act
        List<User> users = userDAO.findAll();

        // Assert
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@example.com", "user2@example.com");
    }

    @Test
    @DisplayName("Получение всех пользователей - возвращает пустой список")
    void findAll_ShouldReturnEmptyList_WhenNoUsers() {
        // Act
        List<User> users = userDAO.findAll();

        // Assert
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("Обновление пользователя - успешно")
    void update_ShouldUpdateUser() {
        // Arrange
        Long userId = userDAO.save(testUser);
        User userToUpdate = userDAO.findById(userId).get();

        // Act
        userToUpdate.setName("Обновленное имя");
        userToUpdate.setEmail("updated@example.com");
        userToUpdate.setAge(35);
        userDAO.update(userToUpdate);

        // Assert
        Optional<User> updatedUser = userDAO.findById(userId);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getName()).isEqualTo("Обновленное имя");
        assertThat(updatedUser.get().getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.get().getAge()).isEqualTo(35);
    }

    @Test
    @DisplayName("Удаление пользователя - успешно")
    void delete_ShouldDeleteUser() {
        // Arrange
        Long userId = userDAO.save(testUser);
        assertThat(userDAO.findById(userId)).isPresent();

        // Act
        userDAO.delete(userId);

        // Assert
        assertThat(userDAO.findById(userId)).isEmpty();
    }

    @Test
    @DisplayName("Удаление пользователя - выбрасывает исключение при несуществующем ID")
    void delete_ShouldThrowException_WhenUserNotFound() {
        // Act & Assert
        assertThatThrownBy(() -> userDAO.delete(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("Поиск пользователя по email - успешно")
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Arrange
        userDAO.save(testUser);

        // Act
        Optional<User> foundUser = userDAO.findByEmail("test@example.com");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Поиск пользователя по email - возвращает пустой Optional")
    void findByEmail_ShouldReturnEmptyOptional_WhenEmailNotExists() {
        // Act
        Optional<User> foundUser = userDAO.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName("Сохранение пользователя - Выбрасывает исключение при не уникальном email")
    void save_ShouldEnforceEmailUniqueness() {
        // Arrange
        userDAO.save(testUser);

        User duplicateUser = User.builder()
                .name("Другой Пользователь")
                .email("test@example.com") // Тот же email
                .age(40)
                .build();

        assertThatThrownBy(() -> userDAO.save(duplicateUser))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка сохранения user");
    }
}