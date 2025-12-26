package ru.astondevs.learn.vorobev.dao;

import jakarta.persistence.NoResultException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.astondevs.learn.vorobev.entity.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

@Slf4j
public class UserDAOHibernateImpl implements UserDAO {

    @Override
    public Long save(@NonNull User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            try {
                session.persist(user);
                transaction.commit();
                log.info("User сохранен с ID: {}", user.getId());
                return user.getId();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                log.error("Ошибка сохранения user", e);
                throw new RuntimeException("Ошибка сохранения user", e);
            }
        }
    }

    @Override
    public Optional<User> findById(@NonNull Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.find(User.class, id);
            log.debug("User найден по ID {}: {}", id, user);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            log.error("Ошибка поиска user по ID: {}", id, e);
            throw new RuntimeException("Ошибка поиска user по ID", e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery("FROM User", User.class);
            List<User> users = query.getResultList();
            log.debug("Найдено {} users", users.size());
            return users;
        } catch (Exception e) {
            log.error("Ошибка получения всех users", e);
            throw new RuntimeException("Ошибка получения всех users", e);
        }
    }

    @Override
    public void update(@NonNull User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            try {
                session.merge(user);
                transaction.commit();
                log.info("User обновлен: {}", user.getId());
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                log.error("Ошибка обновления user: {}", user.getId(), e);
                throw new RuntimeException("Ошибка обновления user", e);
            }
        }
    }

    @Override
    public void delete(@NonNull Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            try {
                Query<?> query = session.createQuery("DELETE FROM User WHERE id = :id");
                query.setParameter("id", id);
                int deletedCount = query.executeUpdate();
                transaction.commit();
                if (deletedCount > 0) {
                    log.info("User удален по ID: {}", id);
                } else {
                    log.warn("User с ID {} не найден для удаления", id);
                    throw new IllegalArgumentException("Пользователь с ID " + id + " не найден");
                }
            } catch (Exception e) {
                if (e instanceof IllegalArgumentException) {
                    throw e;
                }
                if (transaction != null) {
                    transaction.rollback();
                }
                log.error("Ошибка удаления user с ID: {}", id, e);
                throw new RuntimeException("Ошибка удаления user", e);
            }
        }
    }

    @Override
    public Optional<User> findByEmail(@NonNull String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<User> query = session.createQuery(
                    "FROM User WHERE email = :email", User.class
            );
            query.setParameter("email", email);
            User user = query.uniqueResult();
            log.debug("User найден по email {}: {}", email, user);
            return Optional.ofNullable(user);
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Ошибка поиска user по email: {}", email, e);
            throw new RuntimeException("Ошибка поиска user по email", e);
        }
    }
}
