package ru.astondevs.learn.vorobev.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.astondevs.learn.vorobev.dto.CreateUserRequest;
import ru.astondevs.learn.vorobev.dto.UpdateUserRequest;
import ru.astondevs.learn.vorobev.repository.UserRepository;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "Иван Иванов",
                "ivan@example.com",
                25
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(25))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createUser_ShouldReturnBadRequest_WhenEmailInvalid() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "Иван Иванов",
                "invalid-email",
                25
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUser_ShouldReturnConflict_WhenEmailExists() throws Exception {
        // Сначала создаем пользователя
        CreateUserRequest request = new CreateUserRequest(
                "Иван Иванов",
                "duplicate@example.com",
                25
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Пытаемся создать еще одного с таким же email
        CreateUserRequest duplicateRequest = new CreateUserRequest(
                "Петр Петров",
                "duplicate@example.com",
                30
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() throws Exception {
        // Сначала создаем пользователя
        CreateUserRequest createRequest = new CreateUserRequest(
                "Иван Иванов",
                "ivan@example.com",
                25
        );

        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        // Получаем пользователя по ID
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Иван Иванов"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        mockMvc.perform(get("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        // Создаем двух пользователей
        CreateUserRequest user1 = new CreateUserRequest("Иван Иванов", "ivan@example.com", 25);
        CreateUserRequest user2 = new CreateUserRequest("Петр Петров", "petr@example.com", 30);

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user1)));

        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user2)));

        // Получаем всех пользователей
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateUser_ShouldUpdateUserSuccessfully() throws Exception {
        // Создаем пользователя
        CreateUserRequest createRequest = new CreateUserRequest(
                "Иван Иванов",
                "ivan@example.com",
                25
        );

        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        // Обновляем пользователя
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Иван Обновленный",
                "ivan.updated@example.com",
                26
        );

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Иван Обновленный"))
                .andExpect(jsonPath("$.email").value("ivan.updated@example.com"))
                .andExpect(jsonPath("$.age").value(26));
    }

    @Test
    void updateUser_ShouldReturnConflict_WhenEmailAlreadyExists() throws Exception {
        // Создаем первого пользователя
        CreateUserRequest user1Request = new CreateUserRequest(
                "Иван Иванов",
                "ivan@example.com",
                25
        );

        String user1Response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1Request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long user1Id = objectMapper.readTree(user1Response).get("id").asLong();

        // Создаем второго пользователя
        CreateUserRequest user2Request = new CreateUserRequest(
                "Петр Петров",
                "petr@example.com",
                30
        );

        String user2Response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2Request)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long user2Id = objectMapper.readTree(user2Response).get("id").asLong();

        // Пытаемся обновить email второго пользователя на email первого
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null,
                "ivan@example.com",  // Тот же email, что и у первого пользователя
                null
        );

        mockMvc.perform(put("/api/v1/users/{id}", user2Id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("уже существует")));
    }

    @Test
    void updateUser_ShouldReturnBadRequest_WhenEmailInvalid() throws Exception {
        // Создаем пользователя
        CreateUserRequest createRequest = new CreateUserRequest(
                "Иван Иванов",
                "ivan@example.com",
                25
        );

        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        // Пытаемся обновить с невалидным email
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                null,
                "invalid-email",
                null
        );

        mockMvc.perform(put("/api/v1/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void updateUser_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Пытаемся обновить несуществующего пользователя
        UpdateUserRequest updateRequest = new UpdateUserRequest(
                "Новое имя",
                "new@example.com",
                30
        );

        mockMvc.perform(put("/api/v1/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("не найден")));
    }

    @Test
    void deleteUser_ShouldDeleteUserSuccessfully() throws Exception {
        // Создаем пользователя
        CreateUserRequest createRequest = new CreateUserRequest(
                "Иван Иванов",
                "ivan@example.com",
                25
        );

        String response = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long userId = objectMapper.readTree(response).get("id").asLong();

        // Удаляем пользователя
        mockMvc.perform(delete("/api/v1/users/{id}", userId))
                .andExpect(status().isNoContent());

        // Проверяем, что пользователь удален
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserNotExists() throws Exception {
        // Пытаемся удалить несуществующего пользователя
        mockMvc.perform(delete("/api/v1/users/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("не найден")));
    }
}
