package ru.astondevs.learn.vorobev.service;


import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.learn.vorobev.dao.UserDAO;
import ru.astondevs.learn.vorobev.entity.User;

import java.util.List;
import java.util.Optional;

@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
        log.info("UserService инициализирован с DAO: {}", userDAO.getClass().getSimpleName());
    }

    @Override
    public Long createUser(@NonNull String name, @NonNull String email, int age) {
        try {
            Optional<User> existingUser = userDAO.findByEmail(email);
            if (existingUser.isPresent()) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует!");
            }
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .age(age)
                    .build();

            Long id = userDAO.save(user);
            log.info("Создан новый user с ID: {}", id);
            return id;
        } catch (Exception e) {
            log.error("Не удалось создать user", e);
            throw e;
        }
    }

    @Override
    public Optional<User> getUserById(@NonNull Long id) {
        try {
            Optional<User> user = userDAO.findById(id);
            if (user.isPresent()) {
                log.debug("Найден user с ID: {}", id);
            } else {
                log.warn("User не найден по ID: {}", id);
            }
            return user;
        } catch (Exception e) {
            log.error("Не удалось найти user по ID", e);
            throw e;
        }
    }

    @Override
    public List<User> getAllUsers() {
        try {
            return userDAO.findAll();
        } catch (Exception e) {
            log.error("Не удалось получить всех users", e);
            throw e;
        }
    }

    @Override
    public void updateUser(@NonNull Long id, String name, String email, Integer age) {
        try {
            Optional<User> optionalUser = userDAO.findById(id);
            if (optionalUser.isEmpty()) {
                throw new IllegalArgumentException("Пользователь с ID " + id + " не найден!");
            }
            User user = optionalUser.get();
            boolean needsUpdate = false;
            if (name != null && !name.trim().isEmpty() && !name.trim().equals(user.getName())) {
                user.setName(name.trim());
                needsUpdate = true;
            }
            if (email != null && !email.trim().isEmpty() && !email.trim().equals(user.getEmail())) {
                String newEmail = email.trim();
                Optional<User> existingUserWithEmail = userDAO.findByEmail(newEmail);
                if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(id)) {
                    throw new IllegalArgumentException("Пользователь с email " + newEmail + " уже существует!");
                }

                user.setEmail(newEmail);
                needsUpdate = true;
            }
            if (age != null && !age.equals(user.getAge())) {
                if (age <= 0 || age > 150) {
                    throw new IllegalArgumentException("Возраст должен быть в диапазоне от 1 до 150 лет!");
                }
                user.setAge(age);
                needsUpdate = true;
            }
            if (needsUpdate) {
                userDAO.update(user);
                log.info("Пользователь с ID {} успешно обновлен", id);
            } else {
                log.info("Пользователь с ID {} не требует обновления - все данные идентичны", id);
            }

        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации при обновлении пользователя с ID: {}", id, e);
            throw e;
        } catch (Exception e) {
            log.error("Не удалось обновить пользователя с ID: {}", id, e);
            throw new RuntimeException("Ошибка при обновлении пользователя", e);
        }
    }

    @Override
    public void deleteUser(@NonNull Long id) {
        try {
            userDAO.delete(id);
            log.info("Удален user с ID: {}", id);
        } catch (Exception e) {
            log.error("Не удалось удалить user", e);
            throw e;
        }
    }
}
