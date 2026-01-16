package ru.astondevs.learn.vorobev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.astondevs.learn.vorobev.dto.CreateUserRequest;
import ru.astondevs.learn.vorobev.dto.UpdateUserRequest;
import ru.astondevs.learn.vorobev.dto.UserResponse;
import ru.astondevs.learn.vorobev.exception.DuplicateEmailException;
import ru.astondevs.learn.vorobev.exception.ResourceNotFoundException;
import ru.astondevs.learn.vorobev.service.UserService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "Иван Иванов",
                "ivan@example.com",
                25
        );

        UserResponse response = new UserResponse(
                1L,
                "Иван Иванов",
                "ivan@example.com",
                25,
                LocalDateTime.now()
        );

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "", // Пустое имя
                "invalid-email", // Неверный email
                0 // Неверный возраст
        );

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUser_ShouldReturnConflict_WhenDuplicateEmail() throws Exception {
        // Arrange
        CreateUserRequest request = new CreateUserRequest(
                "Иван Иванов",
                "duplicate@example.com",
                25
        );

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new DuplicateEmailException("Email уже существует"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenRequiredFieldsMissing() throws Exception {
        // Arrange - пустой запрос
        CreateUserRequest request = new CreateUserRequest(null, null, null);

        // Act & Assert
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(CreateUserRequest.class));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        // Arrange
        UserResponse response = new UserResponse(
                1L,
                "Иван Иванов",
                "ivan@example.com",
                25,
                LocalDateTime.now()
        );

        when(userService.getUserById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Иван Иванов"));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Arrange
        when(userService.getUserById(999L))
                .thenThrow(new ResourceNotFoundException("Пользователь не найден"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(999L);
    }

    @Test
    void getAllUsers_ShouldReturnUserList() throws Exception {
        // Arrange
        List<UserResponse> users = Arrays.asList(
                new UserResponse(1L, "Иван Иванов", "ivan@example.com", 25, LocalDateTime.now()),
                new UserResponse(2L, "Петр Петров", "petr@example.com", 30, LocalDateTime.now())
        );

        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void updateUser_ShouldUpdateSuccessfully() throws Exception {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest(
                "Иван Обновленный",
                "ivan.updated@example.com",
                26
        );

        UserResponse response = new UserResponse(
                1L,
                "Иван Обновленный",
                "ivan.updated@example.com",
                26,
                LocalDateTime.now()
        );

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Иван Обновленный"))
                .andExpect(jsonPath("$.email").value("ivan.updated@example.com"));

        verify(userService, times(1)).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    void updateUser_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null,
                "existing@example.com", // Email, который уже существует у другого пользователя
                null
        );

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenThrow(new DuplicateEmailException("Пользователь с email existing@example.com уже существует"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Пользователь с email existing@example.com уже существует"));

        verify(userService, times(1)).updateUser(eq(1L), any(UpdateUserRequest.class));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenEmailInvalid() throws Exception {
        // Arrange - невалидный email
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null,
                "invalid-email", // Невалидный формат email
                null
        );

        // Act & Assert - валидация происходит на уровне контроллера до вызова сервиса
        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        // Сервис не должен вызываться при валидационных ошибках
        verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

    @Test
    void updateUser_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Новое имя",
                "new@example.com",
                30
        );

        when(userService.updateUser(eq(999L), any(UpdateUserRequest.class)))
                .thenThrow(new ResourceNotFoundException("Пользователь с ID 999 не найден"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с ID 999 не найден"));

        verify(userService, times(1)).updateUser(eq(999L), any(UpdateUserRequest.class));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenAgeInvalid() throws Exception {
        // Arrange - отрицательный возраст
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null,
                null,
                -5 // Невалидный возраст
        );

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.age").exists());

        verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenAgeTooLarge() throws Exception {
        // Arrange - слишком большой возраст
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null,
                null,
                200 // Превышает максимальное значение
        );

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.age").exists());

        verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenNameTooShort() throws Exception {
        // Arrange - слишком короткое имя
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "И", // Слишком короткое имя (менее 2 символов)
                null,
                null
        );

        // Act & Assert
        mockMvc.perform(put("/api/v1/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());

        verify(userService, never()).updateUser(anyLong(), any(UpdateUserRequest.class));
    }

    @Test
    void deleteUser_ShouldDeleteSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Arrange
        doThrow(new ResourceNotFoundException("Пользователь не найден"))
                .when(userService).deleteUser(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(999L);
    }
}