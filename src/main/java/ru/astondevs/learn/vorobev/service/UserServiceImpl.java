package ru.astondevs.learn.vorobev.service;


import lombok.extern.slf4j.Slf4j;
import ru.astondevs.learn.vorobev.dao.UserDAO;
import ru.astondevs.learn.vorobev.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Slf4j
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO;
    private final Scanner scanner;
    private boolean running;

    public UserServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.scanner = new Scanner(System.in);
        this.running = true;
        log.info("UserService инициализирован с DAO: {}", userDAO.getClass().getSimpleName());
    }

    @Override
    public void createUser() {
        System.out.println("\n=== Создание нового пользователя ===");

        try {
            System.out.print("Введите имя: ");
            String name = scanner.nextLine();

            System.out.print("Введите email: ");
            String email = scanner.nextLine();

            System.out.print("Введите возраст: ");
            int age = Integer.parseInt(scanner.nextLine());

            Optional<User> existingUser = userDAO.findByEmail(email);
            if (existingUser.isPresent()) {
                System.out.println("Ошибка: Пользователь с таким email уже существует!");
                return;
            }
            User user = User.builder()
                    .name(name)
                    .email(email)
                    .age(age)
                    .build();

            Long id = userDAO.save(user);
            System.out.println("✓ Пользователь успешно создан с ID: " + id);
            log.info("Создан новый user с ID: {}", id);

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Возраст должен быть числом!");
            log.warn("Не правильный ввод age");
        } catch (Exception e) {
            System.out.println("Не удалось создать пользователя: " + e.getMessage());
            log.error("Не удалось создать user", e);
        }
    }

    @Override
    public void getUserById() {
        System.out.println("\n=== Найти пользователя по ID ===");

        try {
            System.out.print("Введите ID пользователя: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> user = userDAO.findById(id);
            if (user.isPresent()) {
                displayUser(user.get());
                log.debug("Найден user с ID: {}", id);
            } else {
                System.out.println("✗ Пользователь не найден по ID: " + id);
                log.warn("User не найден по ID: {}", id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом!");
            log.warn("Не правильный ввод ID");
        } catch (Exception e) {
            System.out.println("Ошибка поиска пользователя: " + e.getMessage());
            log.error("Не удалось найти user по ID", e);
        }
    }

    @Override
    public void getAllUsers() {
        System.out.println("\n=== Все пользователи ===");

        try {
            List<User> users = userDAO.findAll();
            if (users.isEmpty()) {
                System.out.println("Пользователи не найдены.");
            } else {
                users.forEach(this::displayUser);
                System.out.println("\nВсего пользователей: " + users.size());
                log.debug("Получено {} users", users.size());
            }
        } catch (Exception e) {
            System.out.println("Ошибка получения users: " + e.getMessage());
            log.error("Не удалось получить всех users", e);
        }
    }

    @Override
    public void updateUser() {
        System.out.println("\n=== Обновить данные пользователя ===");

        try {
            System.out.print("Введите ID пользователя для обновления данных: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> optionalUser = userDAO.findById(id);
            if (optionalUser.isEmpty()) {
                System.out.println("✗ Пользователь не найден по ID: " + id);
                log.warn("Попытка обновить пользователя с не сущестующим ID: {}", id);
                return;
            }

            User user = optionalUser.get();
            System.out.println("Текущие данные пользователя:");
            displayUser(user);
            System.out.println();

            System.out.print("Введите новое имя (нажмите Enter чтобы оставить текущее): ");
            String name = scanner.nextLine();
            if (!name.trim().isEmpty()) {
                user.setName(name);
            }

            System.out.print("Введите новый email (нажмите Enter, чтобы оставить текущий): ");
            String email = scanner.nextLine();
            if (!email.trim().isEmpty()) {
                Optional<User> existingUser = userDAO.findByEmail(email);
                if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                    System.out.println("Ошибка: Email уже используется другим пользователем!");
                    return;
                }
                user.setEmail(email);
            }

            System.out.print("Введите новый возраст (нажмите Enter, чтобы оставить текущий): ");
            String ageInput = scanner.nextLine();
            if (!ageInput.trim().isEmpty()) {
                user.setAge(Integer.parseInt(ageInput));
            }

            userDAO.update(user);
            System.out.println("✓ Пользователь успешно обновлен!");
            log.info("Обновлен user с ID: {}", id);

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Неверный формат числа!");
            log.warn("Не правильный формат числа для обновления");
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении пользователя: " + e.getMessage());
            log.error("Не удалось обновить user", e);
        }
    }

    @Override
    public void deleteUser() {
        System.out.println("\n=== Удаление пользователя ===");

        try {
            System.out.print("Введите ID пользователя для удаления: ");
            Long id = Long.parseLong(scanner.nextLine());

            System.out.print("Вы уверены, что хотите удалить пользователя с ID " + id + "? (Д/Н): ");
            String confirmation = scanner.nextLine();

            if ("Д".equalsIgnoreCase(confirmation)) {
                userDAO.delete(id);
                System.out.println("✓ Пользователь успешно удален!");
                log.info("Удален user с ID: {}", id);
            } else {
                System.out.println("Удаление отменено.");
                log.debug("Удаление user отмененно для ID: {}", id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: ID должен быть числом!");
            log.warn("Неправильный ввод ID для удаления");
        } catch (Exception e) {
            System.out.println("Ошибка при удалении пользователя: " + e.getMessage());
            log.error("Не удалось удалить user", e);
        }
    }

    @Override
    public void run() {
        log.info("Запуск UserService");

        while (running) {
            showMenu();

            try {
                System.out.print("\nВыберите пункт меню (1-6): ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        createUser();
                        break;
                    case "2":
                        getUserById();
                        break;
                    case "3":
                        getAllUsers();
                        break;
                    case "4":
                        updateUser();
                        break;
                    case "5":
                        deleteUser();
                        break;
                    case "6":
                        running = false;
                        System.out.println("Завершение работы приложения...");
                        log.info("Завершение приложения");
                        break;
                    default:
                        System.out.println("Неверный выбор. Пожалуйста, введите число от 1 до 6.");
                        log.warn("Не верный выбор пункта меню: {}", choice);
                }
            } catch (Exception e) {
                System.out.println("Произошла непредвиденная ошибка: " + e.getMessage());
                log.error("Непредвиденная ошибка", e);
            }
        }
    }

    @Override
    public void close() {
        scanner.close();
        log.info("UserService ресурсы закрыты");
    }

    private void showMenu() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("      СИСТЕМА УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ");
        System.out.println("=".repeat(40));
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("6. Выход");
        System.out.println("-".repeat(40));
    }

    private void displayUser(User user) {
        System.out.printf("ID: %-5d | Имя: %-20s | Email: %-25s | Возраст: %-3d | Создан: %s%n",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }
}
