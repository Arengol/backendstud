import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.astondevs.learn.vorobev.dao.UserDAO;
import ru.astondevs.learn.vorobev.entity.User;
import ru.astondevs.learn.vorobev.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit тесты для UserServiceImpl")
class UserServiceImplTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();

        anotherUser = User.builder()
                .id(2L)
                .name("Петр Петров")
                .email("petr@example.com")
                .age(30)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Создание пользователя - успешно")
    void createUser_ShouldReturnUserId_WhenUserCreated() {
        // Arrange
        when(userDAO.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenReturn(1L);

        // Act
        Long userId = userService.createUser("Иван Иванов", "ivan@example.com", 25);

        // Assert
        assertThat(userId).isEqualTo(1L);
        verify(userDAO, times(1)).findByEmail("ivan@example.com");
        verify(userDAO, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - выбрасывает исключение при дублировании email")
    void createUser_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(userDAO.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        assertThatThrownBy(() ->
                userService.createUser("Иван Иванов", "ivan@example.com", 25))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Пользователь с таким email уже существует!");

        verify(userDAO, times(1)).findByEmail("ivan@example.com");
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Получение пользователя по ID - успешно")
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById(1L);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getName()).isEqualTo("Иван Иванов");
        verify(userDAO, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Получение пользователя по ID - возвращает пустой Optional")
    void getUserById_ShouldReturnEmptyOptional_WhenUserNotExists() {
        // Arrange
        when(userDAO.findById(anyLong())).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(999L);

        // Assert
        assertThat(result).isEmpty();
        verify(userDAO, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Получение всех пользователей - успешно")
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        List<User> users = Arrays.asList(testUser, anotherUser);
        when(userDAO.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testUser, anotherUser);
        verify(userDAO, times(1)).findAll();
    }

    @Test
    @DisplayName("Получение всех пользователей - возвращает пустой список")
    void getAllUsers_ShouldReturnEmptyList_WhenNoUsers() {
        // Arrange
        when(userDAO.findAll()).thenReturn(List.of());

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertThat(result).isEmpty();
        verify(userDAO, times(1)).findAll();
    }

    @Test
    @DisplayName("Обновление пользователя - успешно обновляет все поля")
    void updateUser_ShouldUpdateUser_WhenAllFieldsProvided() {
        // Arrange
        User existingUser = User.builder()
                .id(1L)
                .name("Старое имя")
                .email("old@example.com")
                .age(20)
                .build();

        when(userDAO.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDAO.findByEmail("new@example.com")).thenReturn(Optional.empty());
        doNothing().when(userDAO).update(any(User.class));

        // Act
        userService.updateUser(1L, "Новое имя", "new@example.com", 30);

        // Assert
        verify(userDAO, times(1)).findById(1L);
        verify(userDAO, times(1)).findByEmail("new@example.com");
        verify(userDAO, times(1)).update(argThat(user ->
                user.getName().equals("Новое имя") &&
                        user.getEmail().equals("new@example.com") &&
                        user.getAge() == 30
        ));
    }

    @Test
    @DisplayName("Обновление пользователя - частичное обновление")
    void updateUser_ShouldUpdateOnlyProvidedFields() {
        // Arrange
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userDAO).update(any(User.class));

        // Act - обновляем только имя
        userService.updateUser(1L, "Новое имя", null, null);

        // Assert
        verify(userDAO, times(1)).update(argThat(user ->
                user.getName().equals("Новое имя") &&
                        user.getEmail().equals("ivan@example.com") && // осталось прежним
                        user.getAge() == 25 // осталось прежним
        ));
    }

    @Test
    @DisplayName("Обновление пользователя - выбрасывает исключение при дублировании email")
    void updateUser_ShouldThrowException_WhenEmailAlreadyUsedByAnotherUser() {
        // Arrange
        User existingUser = User.builder()
                .id(1L)
                .name("Иван")
                .email("ivan@example.com")
                .age(25)
                .build();

        User otherUser = User.builder()
                .id(2L)
                .name("Другой")
                .email("existing@example.com")
                .age(30)
                .build();

        when(userDAO.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userDAO.findByEmail("existing@example.com")).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThatThrownBy(() ->
                userService.updateUser(1L, null, "existing@example.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже существует");

        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя - выбрасывает исключение при невалидном возрасте")
    void updateUser_ShouldThrowException_WhenInvalidAge() {
        // Arrange
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));

        // Act & Assert - возраст 0
        assertThatThrownBy(() ->
                userService.updateUser(1L, null, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Возраст должен быть в диапазоне");

        // Act & Assert - возраст отрицательный
        assertThatThrownBy(() ->
                userService.updateUser(1L, null, null, -5))
                .isInstanceOf(IllegalArgumentException.class);

        // Act & Assert - возраст слишком большой
        assertThatThrownBy(() ->
                userService.updateUser(1L, null, null, 200))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Обновление пользователя - не выполняет обновление при одинаковых данных")
    void updateUser_ShouldNotUpdate_WhenNoChanges() {
        // Arrange
        when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));

        // Act - передаем те же данные
        userService.updateUser(1L, testUser.getName(), testUser.getEmail(), testUser.getAge());

        // Assert
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    @DisplayName("Удаление пользователя - успешно")
    void deleteUser_ShouldCallDaoDelete() {
        // Arrange
        doNothing().when(userDAO).delete(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userDAO, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Удаление пользователя - выбрасывает исключение при ошибке DAO")
    void deleteUser_ShouldThrowException_WhenDaoFails() {
        // Arrange
        doThrow(new RuntimeException("Ошибка БД"))
                .when(userDAO).delete(1L);

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка БД");
    }
}
