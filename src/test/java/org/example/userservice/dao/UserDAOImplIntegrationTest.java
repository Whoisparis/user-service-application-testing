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
    void setUp() {
        try {
            // Create session factory from hibernate-test.cfg.xml
            sessionFactory = new Configuration().configure("hibernate-test.cfg.xml").buildSessionFactory();
            userDAO = new UserDAOImpl(sessionFactory);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to set up session factory: " + e.getMessage());
        }
    }

    @AfterAll
    void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void clearDatabase() {
        // Clean up before each test
        List<User> users = userDAO.findAll();
        for (User user : users) {
            userDAO.delete(user.getId());
        }
    }

    @Test
    void save_ShouldSaveUserToDatabase() {
        // Arrange
        User user = new User();
        user.setName("Integration Test User");
        user.setEmail("integration@test.com");
        user.setAge(30);

        // Act
        User savedUser = userDAO.save(user);

        // Assert
        assertNotNull(savedUser);
        assertNotNull(savedUser.getId());
        assertEquals("Integration Test User", savedUser.getName());
        assertEquals("integration@test.com", savedUser.getEmail());
        assertEquals(30, savedUser.getAge());
    }

    @Test
    void findById_ShouldReturnUserFromDatabase() {
        // Arrange
        User user = new User();
        user.setName("Find User");
        user.setEmail("find@test.com");
        user.setAge(25);
        User savedUser = userDAO.save(user);

        // Act
        Optional<User> foundUser = userDAO.findById(savedUser.getId());

        // Assert
        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("Find User", foundUser.get().getName());
    }

    @Test
    void findAll_ShouldReturnAllUsersFromDatabase() {
        // Arrange
        User user1 = new User();
        user1.setName("User One");
        user1.setEmail("one@test.com");
        user1.setAge(20);

        User user2 = new User();
        user2.setName("User Two");
        user2.setEmail("two@test.com");
        user2.setAge(30);

        userDAO.save(user1);
        userDAO.save(user2);

        // Act
        List<User> users = userDAO.findAll();

        // Assert
        assertNotNull(users);
        assertEquals(2, users.size());
    }

    @Test
    void update_ShouldUpdateUserInDatabase() {
        // Arrange
        User user = new User();
        user.setName("Original Name");
        user.setEmail("original@test.com");
        user.setAge(25);
        User savedUser = userDAO.save(user);

        // Act
        savedUser.setName("Updated Name");
        savedUser.setEmail("updated@test.com");
        User updatedUser = userDAO.update(savedUser);

        // Assert
        assertNotNull(updatedUser);
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@test.com", updatedUser.getEmail());

        // Verify it's actually updated in database
        Optional<User> foundUser = userDAO.findById(savedUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("Updated Name", foundUser.get().getName());
    }

    @Test
    void delete_ShouldRemoveUserFromDatabase() {
        // Arrange
        User user = new User();
        user.setName("To Delete");
        user.setEmail("delete@test.com");
        user.setAge(35);
        User savedUser = userDAO.save(user);

        // Act
        userDAO.delete(savedUser.getId());

        // Assert
        Optional<User> foundUser = userDAO.findById(savedUser.getId());
        assertFalse(foundUser.isPresent());
    }

    @Test
    void existsByEmail_ShouldReturnTrueForExistingEmail() {
        // Arrange
        User user = new User();
        user.setName("Email Test");
        user.setEmail("exists@test.com");
        user.setAge(40);
        userDAO.save(user);

        // Act
        boolean exists = userDAO.existsByEmail("exists@test.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_ShouldReturnFalseForNonExistingEmail() {
        // Act
        boolean exists = userDAO.existsByEmail("nonexistent@test.com");

        // Assert
        assertFalse(exists);
    }
}

