import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import ru.astondevs.learn.vorobev.dto.CreateUserRequest;
import ru.astondevs.learn.vorobev.dto.UpdateUserRequest;
import ru.astondevs.learn.vorobev.dto.UserEvent;
import ru.astondevs.learn.vorobev.dto.UserResponse;
import ru.astondevs.learn.vorobev.entity.User;
import ru.astondevs.learn.vorobev.exception.DuplicateEmailException;
import ru.astondevs.learn.vorobev.exception.ResourceNotFoundException;
import ru.astondevs.learn.vorobev.repository.UserRepository;
import ru.astondevs.learn.vorobev.service.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, UserEvent> kafkaTemplate;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "Иван Иванов",
                "ivan@example.com",
                25
        );

        User savedUser = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.existsByEmail("ivan@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.createUser(request);

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Иван Иванов");
        assertThat(response.getEmail()).isEqualTo("ivan@example.com");
        assertThat(response.getAge()).isEqualTo(25);

        verify(userRepository, times(1)).existsByEmail("ivan@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_ShouldThrowException_WhenEmailExists() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "Иван Иванов",
                "existing@example.com",
                25
        );

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("уже существует");

        verify(userRepository, times(1)).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getUserById(1L);

        // Assert
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("Иван Иванов");
        assertThat(response.getEmail()).isEqualTo("ivan@example.com");

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotExists() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user1 = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .age(25)
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Петр Петров")
                .email("petr@example.com")
                .age(30)
                .build();

        List<User> users = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<UserResponse> responses = userService.getAllUsers();

        // Assert
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(1).getId()).isEqualTo(2L);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void updateUser_ShouldUpdateUserSuccessfully() {
        // Arrange
        User existingUser = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .age(25)
                .build();

        UpdateUserRequest request = new UpdateUserRequest(
                "Иван Обновленный",
                "ivan.updated@example.com",
                26
        );

        User updatedUser = User.builder()
                .id(1L)
                .name("Иван Обновленный")
                .email("ivan.updated@example.com")
                .age(26)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("ivan.updated@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserResponse response = userService.updateUser(1L, request);

        // Assert
        assertThat(response.getName()).isEqualTo("Иван Обновленный");
        assertThat(response.getEmail()).isEqualTo("ivan.updated@example.com");
        assertThat(response.getAge()).isEqualTo(26);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findByEmail("ivan.updated@example.com");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(
                "Иван Обновленный",
                "ivan.updated@example.com",
                26
        );

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(999L, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowException_WhenDuplicateEmail() {
        // Arrange
        User existingUser = User.builder()
                .id(1L)
                .name("Иван Иванов")
                .email("ivan@example.com")
                .age(25)
                .build();

        User otherUser = User.builder()
                .id(2L)
                .name("Другой Пользователь")
                .email("existing@example.com")
                .age(30)
                .build();

        UpdateUserRequest request = new UpdateUserRequest(
                null,
                "existing@example.com",
                null
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(otherUser));

        // Act & Assert
        assertThatThrownBy(() -> userService.updateUser(1L, request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("уже существует");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteSuccessfully() {
        // Arrange
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .age(25)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(userId);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).deleteById(userId);
        verify(kafkaTemplate, times(1)).send(anyString(), any(UserEvent.class));
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("не найден");

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).deleteById(anyLong());
        verify(kafkaTemplate, never()).send(anyString(), any());
    }
}