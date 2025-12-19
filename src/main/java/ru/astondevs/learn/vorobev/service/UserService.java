package ru.astondevs.learn.vorobev.service;

public interface UserService {
    void createUser();

    void getUserById();

    void getAllUsers();

    void updateUser();

    void deleteUser();

    void run();

    void close();
}
