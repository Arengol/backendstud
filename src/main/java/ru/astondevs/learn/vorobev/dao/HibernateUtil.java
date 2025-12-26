package ru.astondevs.learn.vorobev.dao;

import lombok.Getter;
import ru.astondevs.learn.vorobev.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

@Slf4j
public class HibernateUtil {
    @Getter
    private static SessionFactory sessionFactory;

    public static void init(String url, String username, String password) {
        try {
            String configFile = System.getProperty("hibernate.config.file", "hibernate.cfg.xml");
            StandardServiceRegistry standardRegistry;
            if (url == null || username == null || password == null) {
                 standardRegistry = new StandardServiceRegistryBuilder()
                        .configure(configFile)
                        .build();
            }
            else {
                 standardRegistry = new StandardServiceRegistryBuilder()
                        .configure(configFile)
                        .applySetting("hibernate.connection.url", url)
                        .applySetting("hibernate.connection.username", username)
                        .applySetting("hibernate.connection.password", password)
                        .build();
            }
            Metadata metadata = new MetadataSources(standardRegistry)
                    .addAnnotatedClass(User.class)
                    .getMetadataBuilder()
                    .build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();
            log.info("Hibernate SessionFactory успешно создана");

        } catch (Exception e) {
            log.error("Ошибка создания Hibernate SessionFactory", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            log.info("Hibernate SessionFactory закрыта");
        }
    }
}
