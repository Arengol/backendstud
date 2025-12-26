package ru.astondevs.learn.vorobev.dao;

import ru.astondevs.learn.vorobev.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Long save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void update(User user);

    void delete(Long id);

    Optional<User> findByEmail(String email);
}
