package ru.astondevs.learn.vorobev;

import lombok.extern.slf4j.Slf4j;
import ru.astondevs.learn.vorobev.dao.HibernateUtil;
import ru.astondevs.learn.vorobev.dao.UserDAO;
import ru.astondevs.learn.vorobev.dao.UserDAOHibernateImpl;
import ru.astondevs.learn.vorobev.service.UserService;
import ru.astondevs.learn.vorobev.service.UserServiceImpl;

@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("Запуск приложения");

        UserService userService = null;

        try {
            UserDAO userDAO = new UserDAOHibernateImpl();
            userService = new UserServiceImpl(userDAO);
            userService.run();

        } catch (Exception e) {
            log.error("Ошибка", e);
            System.err.println("Получена ошибка: " + e.getMessage());
        } finally {
            if (userService != null) {
                userService.close();
            }
            HibernateUtil.shutdown();
            log.info("Приложение остановлено");
        }
    }
}