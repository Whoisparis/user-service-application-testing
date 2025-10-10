package org.example.userservice.dao;

import org.example.userservice.entity.User;
import org.example.userservice.exception.UserNotFoundException;
import org.example.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

public class UserDAOImpl implements UserDAO {

    private static final Logger logger = LogManager.getLogger(UserDAOImpl.class);
    private final SessionFactory sessionFactory;

    public UserDAOImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    public UserDAOImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public User save(User user) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            logger.debug("Saving user: {}", user.getEmail());

            session.persist(user);
            transaction.commit();

            logger.info("User saved successfully with id: {}", user.getId());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving user: {}", user.getEmail(), e);
            throw new RuntimeException("Error saving user: " + user.getEmail(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            logger.debug("Finding user by id: {}", id);
            User user = session.get(User.class, id);

            if (user != null) {
                logger.debug("User found: {}", user.getEmail());
            } else {
                logger.debug("User not found: {}", id);
            }
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by id: {}", id, e);
            throw new RuntimeException("Error finding user by id: " + id, e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            logger.debug("Finding all users");
            Query<User> query = session.createQuery("FROM User", User.class);
            List<User> users = query.list();

            logger.debug("Users found: {}", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error finding all users", e);
            throw new RuntimeException("Error finding all users", e);
        }
    }

    @Override
    public User update(User user) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            logger.debug("Updating user: {}", user.getEmail());

            session.merge(user);
            transaction.commit();

            logger.info("User updated successfully with id: {}", user.getEmail());
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating user: {}", user.getEmail(), e);
            throw new RuntimeException("Error updating user: " + user.getEmail(), e);
        }
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            logger.debug("Deleting user: {}", id);

            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                transaction.commit();
                logger.info("User deleted successfully with id: {}", id);
            } else {
                transaction.rollback();
                logger.warn("User not found: {}", id);
                throw new UserNotFoundException("User not found: " + id);
            }
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            if  (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user: {}", id, e);
            throw new RuntimeException("Error deleting user: " + id, e);
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            logger.debug("Finding user by email: {}", email);
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);

            User user = query.uniqueResult();
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", email, e);
            throw new RuntimeException("Error finding user by email: " + email, e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            logger.debug("Checking if email exists: {}", email);
            Query<Long> query = session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class);
            query.setParameter("email", email);

            Long count = query.uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            logger.error("Error checking if email exists: {}", email, e);
            throw new RuntimeException("Error checking if email exists: " + email, e);
        }
    }
}