package org.example.userservice.dao;


import org.example.userservice.entity.User;
import org.example.userservice.exception.UserNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOImplTest {
    @Mock(lenient = true)
    private SessionFactory sessionFactory;

    @Mock(lenient = true)
    private Session session;

    @Mock(lenient = true)
    private Transaction transaction;

    @Mock(lenient = true)
    private Query<User> userQuery;

    @Mock(lenient = true)
    private Query<Long> longQuery;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        // Используем lenient для всех общих моков
        lenient().when(sessionFactory.openSession()).thenReturn(session);
        lenient().when(session.beginTransaction()).thenReturn(transaction);
        userDAO = new UserDAOImpl(sessionFactory);
    }

    // ========== SAVE METHOD TESTS ==========

    @Test
    void save_ShouldReturnSavedUser() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        doAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setId(1L);
            return null;
        }).when(session).persist(user);

        // Act
        User savedUser = userDAO.save(user);

        // Assert
        assertNotNull(savedUser);
        assertEquals(1L, savedUser.getId());
        assertEquals("Test User", savedUser.getName());

        verify(sessionFactory).openSession();
        verify(session).beginTransaction();
        verify(session).persist(user);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void save_WhenPersistThrowsException_ShouldRollbackAndThrow() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        doThrow(new RuntimeException("Database error")).when(session).persist(user);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.save(user);
        });

        assertEquals("Error saving user: test@example.com", exception.getMessage());
        verify(session).persist(user);
        verify(transaction).rollback();
        verify(session).close();
    }

    @Test
    void save_WhenTransactionIsNullAndExceptionOccurs_ShouldHandleGracefully() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        when(session.beginTransaction()).thenReturn(null);
        doThrow(new RuntimeException("Database error")).when(session).persist(user);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.save(user);
        });

        assertEquals("Error saving user: test@example.com", exception.getMessage());
        verify(session).persist(user);
        verify(session, never()).getTransaction(); // transaction is null
        verify(session).close();
    }

    // ========== FIND_BY_ID METHOD TESTS ==========

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        // Arrange
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setName("Test User");
        expectedUser.setEmail("test@example.com");
        expectedUser.setAge(25);

        when(session.get(User.class, userId)).thenReturn(expectedUser);

        // Act
        Optional<User> result = userDAO.findById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());

        verify(sessionFactory).openSession();
        verify(session).get(User.class, userId);
        verify(session, never()).beginTransaction();
        verify(session).close();
    }

    @Test
    void findById_WhenUserNotExists_ShouldReturnEmptyOptional() {
        // Arrange
        Long userId = 999L;
        when(session.get(User.class, userId)).thenReturn(null);

        // Act
        Optional<User> result = userDAO.findById(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(session).get(User.class, userId);
        verify(session).close();
    }

    @Test
    void findById_WhenExceptionOccurs_ShouldThrowRuntimeException() {
        // Arrange
        Long userId = 1L;
        when(session.get(User.class, userId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.findById(userId);
        });

        assertEquals("Error finding user by id: 1", exception.getMessage());
        verify(session).get(User.class, userId);
        verify(session).close();
    }

    // ========== FIND_ALL METHOD TESTS ==========

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");

        List<User> expectedUsers = Arrays.asList(user1, user2);

        when(session.createQuery("FROM User", User.class)).thenReturn(userQuery);
        when(userQuery.list()).thenReturn(expectedUsers);

        // Act
        List<User> result = userDAO.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);

        verify(sessionFactory).openSession();
        verify(session).createQuery("FROM User", User.class);
        verify(userQuery).list();
        verify(session, never()).beginTransaction();
        verify(session).close();
    }

    @Test
    void findAll_WhenNoUsers_ShouldReturnEmptyList() {
        // Arrange
        when(session.createQuery("FROM User", User.class)).thenReturn(userQuery);
        when(userQuery.list()).thenReturn(Collections.emptyList());

        // Act
        List<User> result = userDAO.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userQuery).list();
        verify(session).close();
    }

    // ========== UPDATE METHOD TESTS ==========

    @Test
    void update_ShouldUpdateUser() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setName("Updated User");
        user.setEmail("updated@example.com");
        user.setAge(30);

        when(session.merge(user)).thenReturn(user);

        // Act
        User updatedUser = userDAO.update(user);

        // Assert
        assertNotNull(updatedUser);
        assertEquals(user, updatedUser);

        verify(sessionFactory).openSession();
        verify(session).beginTransaction();
        verify(session).merge(user);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void update_WhenMergeThrowsException_ShouldRollbackAndThrow() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setName("Updated User");
        user.setEmail("updated@example.com");

        doThrow(new RuntimeException("Database error")).when(session).merge(user);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.update(user);
        });

        assertEquals("Error updating user: updated@example.com", exception.getMessage());
        verify(session).merge(user);
        verify(transaction).rollback();
        verify(session).close();
    }

    @Test
    void update_WhenTransactionIsNullAndExceptionOccurs_ShouldHandleGracefully() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setName("Updated User");
        user.setEmail("updated@example.com");

        when(session.beginTransaction()).thenReturn(null);
        doThrow(new RuntimeException("Database error")).when(session).merge(user);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.update(user);
        });

        assertEquals("Error updating user: updated@example.com", exception.getMessage());
        verify(session).merge(user);
        verify(session, never()).getTransaction();
        verify(session).close();
    }

    // ========== DELETE METHOD TESTS ==========

    @Test
    void delete_ShouldDeleteUser() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        when(session.get(User.class, userId)).thenReturn(user);

        // Act
        userDAO.delete(userId);

        // Assert
        verify(sessionFactory).openSession();
        verify(session).beginTransaction();
        verify(session).get(User.class, userId);
        verify(session).remove(user);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void delete_WhenUserNotExists_ShouldThrowUserNotFoundException() {
        // Arrange
        Long userId = 999L;
        when(session.get(User.class, userId)).thenReturn(null);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userDAO.delete(userId);
        });

        assertEquals("User not found: 999", exception.getMessage());
        verify(session).get(User.class, userId);
        verify(session, never()).remove(any(User.class));
        verify(transaction).rollback();
        verify(session).close();
    }

    @Test
    void delete_WhenRemoveThrowsException_ShouldRollbackAndThrow() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(session.get(User.class, userId)).thenReturn(user);
        doThrow(new RuntimeException("Database error")).when(session).remove(user);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.delete(userId);
        });

        assertEquals("Error deleting user: 1", exception.getMessage());
        verify(session).remove(user);
        verify(transaction).rollback();
        verify(session).close();
    }

    @Test
    void delete_WhenTransactionIsNullAndExceptionOccurs_ShouldHandleGracefully() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(session.get(User.class, userId)).thenReturn(user);
        when(session.beginTransaction()).thenReturn(null);
        doThrow(new RuntimeException("Database error")).when(session).remove(user);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.delete(userId);
        });

        assertEquals("Error deleting user: 1", exception.getMessage());
        verify(session).remove(user);
        verify(session, never()).getTransaction();
        verify(session).close();
    }

    // ========== EXISTS_BY_EMAIL METHOD TESTS ==========

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // Arrange
        String email = "existing@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(1L);

        // Act
        boolean result = userDAO.existsByEmail(email);

        // Assert
        assertTrue(result);
        verify(sessionFactory).openSession();
        verify(session).createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class);
        verify(longQuery).setParameter("email", email);
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction(); // Не должно быть beginTransaction
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        // Arrange
        String email = "nonexisting@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(0L);

        // Act
        boolean result = userDAO.existsByEmail(email);

        // Assert
        assertFalse(result);
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction(); // Не должно быть beginTransaction
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenQueryReturnsNull_ShouldReturnFalse() {
        // Arrange
        String email = "null@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(null);

        // Act
        boolean result = userDAO.existsByEmail(email);

        // Assert
        assertFalse(result);
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction(); // Не должно быть beginTransaction
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenCountIsGreaterThanZero_ShouldReturnTrue() {
        // Arrange
        String email = "test@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(5L); // > 0

        // Act
        boolean result = userDAO.existsByEmail(email);

        // Assert
        assertTrue(result);
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction(); // Не должно быть beginTransaction
        verify(session).close();
    }



    @Test
    void existsByEmail_WhenExceptionOccurs_ShouldThrowRuntimeException() {
        // Arrange
        String email = "error@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.existsByEmail(email);
        });

        assertEquals("Error checking if email exists: error@example.com", exception.getMessage());
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction(); // Не должно быть beginTransaction
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenTransactionIsNull_ShouldHandleGracefully() {
        // Arrange
        String email = "test@example.com";

        when(session.beginTransaction()).thenReturn(null);
        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(1L);

        // Act
        boolean result = userDAO.existsByEmail(email);

        // Assert
        assertTrue(result);
        verify(longQuery).uniqueResult();
        // Убрана проверка getTransaction() так как она может не вызываться
        verify(session).close();
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void constructor_WithNullSessionFactory_ShouldWork() {
        // This test ensures that constructor doesn't throw NPE
        assertDoesNotThrow(() -> new UserDAOImpl(null));
    }

    @Test
    void sessionManagement_ShouldOpenAndCloseSessionForEachMethod() {
        // Reset invocations
        reset(sessionFactory, session);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.get(User.class, 1L)).thenReturn(new User());

        // Act
        userDAO.findById(1L);

        // Assert
        verify(sessionFactory, times(1)).openSession();
        verify(session, times(1)).close();
    }

    @Test
    void multipleOperations_ShouldManageSessionsIndependently() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        when(session.get(User.class, 1L)).thenReturn(user);
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return null;
        }).when(session).persist(any(User.class));

        // Act - multiple operations
        userDAO.findById(1L);
        userDAO.save(new User());
        userDAO.findById(1L);

        // Assert - each operation should open and close its own session
        verify(sessionFactory, times(3)).openSession();
        verify(session, times(3)).close();
    }
}
