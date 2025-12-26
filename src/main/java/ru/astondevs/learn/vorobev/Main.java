package ru.astondevs.learn.vorobev;

import lombok.extern.slf4j.Slf4j;
import ru.astondevs.learn.vorobev.dao.HibernateUtil;
import ru.astondevs.learn.vorobev.dao.UserDAO;
import ru.astondevs.learn.vorobev.dao.UserDAOHibernateImpl;
import ru.astondevs.learn.vorobev.entity.User;
import ru.astondevs.learn.vorobev.service.UserService;
import ru.astondevs.learn.vorobev.service.UserServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Slf4j
public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static UserService userService;


    public static void main(String[] args) {
        log.info("Запуск приложения");

        try {
            UserDAO userDAO = new UserDAOHibernateImpl();
            userService = new UserServiceImpl(userDAO);
            run();

        } catch (Exception e) {
            log.error("Ошибка", e);
            System.err.println("Получена ошибка: " + e.getMessage());
        } finally {
            scanner.close();
            HibernateUtil.shutdown();
            log.info("Приложение остановлено");
        }
    }

    private static void showMenu() {
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

    private static void displayUser(User user) {
        System.out.printf("ID: %-5d | Имя: %-20s | Email: %-25s | Возраст: %-3d | Создан: %s%n",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt()
        );
    }

    private static void run() {
        log.info("Запуск UserService");

        while (true) {
            showMenu();

            try {
                System.out.print("\nВыберите пункт меню (1-6): ");
                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        handleCreateUser();
                        break;
                    case "2":
                        handleGetUserById();
                        break;
                    case "3":
                        handleGetAllUsers();
                        break;
                    case "4":
                        handleUpdateUser();
                        break;
                    case "5":
                        handleDeleteUser();
                        break;
                    case "6":
                        System.out.println("Завершение работы приложения...");
                        log.info("Завершение приложения");
                        return;
                    default:
                        System.out.println("Неверный выбор. Пожалуйста, введите число от 1 до 6.");
                        log.warn("Не верный выбор пункта меню: {}", choice);
                        break;
                }
            } catch (Exception e) {
                System.out.println("Произошла непредвиденная ошибка: " + e.getMessage());
                log.error("Непредвиденная ошибка", e);
            }
        }
    }

    private static void handleCreateUser() {
        System.out.println("\n=== Создание нового пользователя ===");

        try {
            System.out.print("Введите имя: ");
            String name = scanner.nextLine();

            System.out.print("Введите email: ");
            String email = scanner.nextLine();

            System.out.print("Введите возраст: ");
            int age = Integer.parseInt(scanner.nextLine());

            Long id = userService.createUser(name, email, age);
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

    private static void handleGetUserById() {
        System.out.println("\n=== Найти пользователя по ID ===");

        try {
            System.out.print("Введите ID пользователя: ");
            Long id = Long.parseLong(scanner.nextLine());

            Optional<User> user = userService.getUserById(id);
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

    private static void handleGetAllUsers() {
        System.out.println("\n=== Все пользователи ===");

        try {
            List<User> users = userService.getAllUsers();
            if (users.isEmpty()) {
                System.out.println("Пользователи не найдены.");
            } else {
                users.forEach(Main::displayUser);
                System.out.println("\nВсего пользователей: " + users.size());
                log.debug("Получено {} users", users.size());
            }
        } catch (Exception e) {
            System.out.println("Ошибка получения users: " + e.getMessage());
            log.error("Не удалось получить всех users", e);
        }
    }

    private static void handleUpdateUser() {
        System.out.println("\n=== Обновить данные пользователя ===");

        try {
            System.out.print("Введите ID пользователя для обновления данных: ");
            Long id = Long.parseLong(scanner.nextLine());

            System.out.print("Введите новое имя (нажмите Enter чтобы оставить текущее): ");
            String name = scanner.nextLine();

            System.out.print("Введите новый email (нажмите Enter, чтобы оставить текущий): ");
            String email = scanner.nextLine();

            System.out.print("Введите новый возраст (нажмите Enter, чтобы оставить текущий): ");
            String ageInput = scanner.nextLine();
            Integer age = ageInput.trim().isEmpty() ? null : Integer.parseInt(ageInput);

            userService.updateUser(id, name, email, age);
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

    private static void handleDeleteUser() {
        System.out.println("\n=== Удаление пользователя ===");

        try {
            System.out.print("Введите ID пользователя для удаления: ");
            Long id = Long.parseLong(scanner.nextLine());

            System.out.print("Вы уверены, что хотите удалить пользователя с ID " + id + "? (Д/Н): ");
            String confirmation = scanner.nextLine();

            if ("Д".equalsIgnoreCase(confirmation)) {
                userService.deleteUser(id);
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
}