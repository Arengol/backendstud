package ru.astondevs.learn.vorobev.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.astondevs.learn.vorobev.dto.CreateUserRequest;
import ru.astondevs.learn.vorobev.dto.UpdateUserRequest;
import ru.astondevs.learn.vorobev.dto.UserResponse;
import ru.astondevs.learn.vorobev.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Методы для работы с данными пользователей")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя и отправляет событие в Kafka")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "21", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "409", description = "Email уже существует")
    })
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);

        response.add(linkTo(methodOn(UserController.class).getUserById(response.getId())).withSelfRel());
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users_list"));
        response.add(linkTo(methodOn(UserController.class).updateUser(response.getId(), null)).withRel("update"));
        response.add(linkTo(methodOn(UserController.class).deleteUser(response.getId())).withRel("delete"));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID", description = "Возвращает данные пользователя и навигационные ссылки")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);

        response.add(linkTo(methodOn(UserController.class).getUserById(response.getId())).withSelfRel());
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users_list"));
        response.add(linkTo(methodOn(UserController.class).updateUser(response.getId(), null)).withRel("update"));
        response.add(linkTo(methodOn(UserController.class).deleteUser(response.getId())).withRel("delete"));

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей", description = "Возвращает полный список зарегистрированных пользователей")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();

        users.forEach(user -> {
                    user.add(linkTo(methodOn(UserController.class).getUserById(user.getId())).withSelfRel());
                    user.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users_list"));
                    user.add(linkTo(methodOn(UserController.class).updateUser(user.getId(), null)).withRel("update"));
                    user.add(linkTo(methodOn(UserController.class).deleteUser(user.getId())).withRel("delete"));
                }
        );

        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить данные пользователя", description = "Изменяет данные существующего пользователя")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse response = userService.updateUser(id, request);

        response.add(linkTo(methodOn(UserController.class).getUserById(response.getId())).withSelfRel());
        response.add(linkTo(methodOn(UserController.class).getAllUsers()).withRel("users_list"));
        response.add(linkTo(methodOn(UserController.class).updateUser(response.getId(), null)).withRel("update"));
        response.add(linkTo(methodOn(UserController.class).deleteUser(response.getId())).withRel("delete"));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить пользователя", description = "Удаляет запись из БД и отправляет DELETE событие в Kafka")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
