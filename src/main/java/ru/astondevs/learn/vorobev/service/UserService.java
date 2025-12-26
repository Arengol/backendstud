package ru.astondevs.learn.vorobev.service;

import ru.astondevs.learn.vorobev.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Long createUser(String name, String email, int age);

    Optional<User> getUserById(Long id);

    List<User> getAllUsers();

    void updateUser(Long id, String name, String email, Integer age);

    void deleteUser(Long id);
}
