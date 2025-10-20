package org.example.userservice.dao;

import org.example.userservice.entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserDAOImplIntegrationTest {

    private SessionFactory sessionFactory;
    private UserDAO userDAO;

    @BeforeAll
    void setUpAll() {
        try {
            Configuration configuration = new Configuration();
            configuration.configure("hibernate.cfg.xml");
            sessionFactory = configuration.buildSessionFactory();
            userDAO = new UserDAOImpl(sessionFactory);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize Hibernate SessionFactory", e);
        }
    }

    @BeforeEach
    void setUp() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.createQuery("DELETE FROM User").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    void tearDownAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    void save_ShouldPersistUserWithGeneratedId() {

        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        User savedUser = userDAO.save(user);

        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("Test User", savedUser.getName());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals(25, savedUser.getAge());
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);
        User savedUser = userDAO.save(user);
        Long userId = savedUser.getId();

        Optional<User> foundUser = userDAO.findById(userId);

        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        assertEquals("Test User", foundUser.get().getName());
    }

    @Test
    void findById_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> foundUser = userDAO.findById(999L);

        assertFalse(foundUser.isPresent());
    }

    @Test
    void findByEmail_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);
        userDAO.save(user);

        Optional<User> foundUser = userDAO.findByEmail("test@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("test@example.com", foundUser.get().getEmail());
        assertEquals("Test User", foundUser.get().getName());
    }

    @Test
    void findByEmail_WhenUserNotExists_ShouldReturnEmpty() {
        Optional<User> foundUser = userDAO.findByEmail("nonexistent@example.com");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void existsByEmail_WhenUserExists_ShouldReturnTrue() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);
        userDAO.save(user);

        boolean exists = userDAO.existsByEmail("test@example.com");

        assertTrue(exists);
    }

    @Test
    void existsByEmail_WhenUserNotExists_ShouldReturnFalse() {
        boolean exists = userDAO.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        user1.setAge(25);
        userDAO.save(user1);

        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        user2.setAge(30);
        userDAO.save(user2);

        List<User> users = userDAO.findAll();

        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    void update_ShouldModifyExistingUser() {
        User user = new User();
        user.setName("Original Name");
        user.setEmail("original@example.com");
        user.setAge(25);
        User savedUser = userDAO.save(user);

        savedUser.setName("Updated Name");
        savedUser.setEmail("updated@example.com");
        savedUser.setAge(30);
        User updatedUser = userDAO.update(savedUser);

        assertNotNull(updatedUser);
        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());
    }

    @Test
    void delete_ShouldRemoveUserFromDatabase() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);
        User savedUser = userDAO.save(user);
        Long userId = savedUser.getId();

        assertTrue(userDAO.findById(userId).isPresent());

        userDAO.delete(userId);

        Optional<User> deletedUser = userDAO.findById(userId);
        assertFalse(deletedUser.isPresent());
    }
}

