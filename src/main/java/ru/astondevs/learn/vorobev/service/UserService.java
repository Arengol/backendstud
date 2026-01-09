package ru.astondevs.learn.vorobev.service;

import ru.astondevs.learn.vorobev.dto.CreateUserRequest;
import ru.astondevs.learn.vorobev.dto.UpdateUserRequest;
import ru.astondevs.learn.vorobev.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, UpdateUserRequest request);
    void deleteUser(Long id);
}
