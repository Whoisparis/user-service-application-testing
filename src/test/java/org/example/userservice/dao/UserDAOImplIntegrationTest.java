package org.example.userservice.dao;

import org.example.userservice.config.TestDatabaseConfig;
import org.example.userservice.entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserDAOImplIntegrationTest {

    private SessionFactory sessionFactory;
    private UserDAO userDAO;

    @BeforeAll
    void setUp(){
        sessionFactory = TestDatabaseConfig.getSessionFactory();
        userDAO = new UserDAOImpl(sessionFactory);
    }

    @AfterAll
    void tearDown(){
        TestDatabaseConfig.shutdown();
    }

    @BeforeEach
    void clearDatabase() {
        Transaction transaction = null;
        try(Session session = sessionFactory.openSession()){
            transaction = session.beginTransaction();
            session.createMutationQuery("DELETE FROM User").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Failed to clear database",e);
        }
    }

    @Test
    void testSaveUser_ShouldPersistUser(){
        User user = new User("Josh Livo", "josh@example.com",25);
        User savedUser = userDAO.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("Josh Livo", savedUser.getName());
        assertEquals("josh@example.com", savedUser.getEmail());
        assertEquals(25, savedUser.getAge());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void testFindUserById_WhenUserExists_ShouldReturnUser(){

        User user = new User("Mark Livo", "mark@example.com", 25);
        User savedUser = userDAO.save(user);

        Optional<User> foundUser = userDAO.findById(savedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals(savedUser.getId(), foundUser.get().getId());
        assertEquals("Mark Livo", foundUser.get().getName());
    }

    @Test
    void testFindUserById_WhenUserNotExists_ShouldReturnEmpty() {

        Optional<User> foundUser = userDAO.findById(999L);

        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindAll_WhenNoUsers_ShouldReturnEmptyList(){
        List<User> users = userDAO.findAll();

        assertTrue(users.isEmpty());
    }

    @Test
    void testFindAll_WhenUserExist_ShouldReturnAllUsers() {

        userDAO.save(new User("User1", "user1@example.com",30));
        userDAO.save(new User("User2","user2@example.com", 25));

        List<User> users = userDAO.findAll();

        assertEquals(2, users.size());
    }

    @Test
    void testUpdateUser_ShouldUpdateUserData(){

        User user = new User("Old name", "old@example.com", 25);
        User savedUser = userDAO.save(user);

        savedUser.setName("New name");
        savedUser.setEmail("new@example.com");
        savedUser.setAge(30);
        User updateUser = userDAO.update(savedUser);

        assertEquals("New name", updateUser.getName());
        assertEquals("new@example.com", updateUser.getEmail());
        assertEquals(30, updateUser.getAge());
    }

    @Test
    void testDeleteUser_ShouldDeleteUser(){

        User user = new User("To delete", "delete@example.com", 25);
        User savedUser = userDAO.save(user);

        userDAO.delete(savedUser.getId());

        Optional<User> deletedUser = userDAO.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testFindByEmail_WhenUserExists_ShouldReturnUser(){

        User user = new User("Email user", "email@example.com", 25);
        userDAO.save(user);

        Optional<User> foundUser = userDAO.findByEmail("email@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals("Email user", foundUser.get().getName());
    }

    @Test
    void testExistsByEmail_WhenEmailExists_ShouldReturnTrue(){

        userDAO.save(new User("Test user", "exists@example.com", 25));

        boolean exists = userDAO.existsByEmail("exists@example.com");

        assertTrue(exists);
    }

    @Test
    void testExistsByEmail_WhenEmailNotExists_ShouldReturnFalse(){

        boolean exists = userDAO.existsByEmail("noneexistent@example.com");

        assertFalse(exists);
    }
}
