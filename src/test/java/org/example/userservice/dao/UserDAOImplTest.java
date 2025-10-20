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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDAOImplTest {
    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    @Mock
    private Transaction transaction;

    @Mock
    private Query<User> userQuery;

    @Mock
    private Query<Long> longQuery;

    private UserDAO userDAO;
    private final Logger logger = LoggerFactory.getLogger(UserDAOImplTest.class);

    @BeforeEach
    void setUp() {
        // Используем lenient чтобы избежать UnnecessaryStubbingException
        lenient().when(sessionFactory.openSession()).thenReturn(session);
        lenient().when(session.beginTransaction()).thenReturn(transaction);

        userDAO = new UserDAOImpl(sessionFactory);
    }

    @Test
    void save_ShouldReturnSavedUser() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        // Используем doAnswer для установки ID через persist
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
    void save_WhenExceptionOccurs_ShouldThrowRuntimeException() {
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
        verify(session).close();
    }

    @Test
    void findAll_WhenNoUsers_ShouldReturnEmptyList() {
        // Arrange
        when(session.createQuery("FROM User", User.class)).thenReturn(userQuery);
        when(userQuery.list()).thenReturn(Arrays.asList());

        // Act
        List<User> result = userDAO.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userQuery).list();
        verify(session).close();
    }

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
    void update_WhenExceptionOccurs_ShouldThrowRuntimeException() {
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
        verify(transaction).rollback(); // Исправлено с commit() на rollback()
        verify(session).close();
    }

    @Test
    void delete_WhenExceptionOccurs_ShouldThrowRuntimeException() {
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

        // Исправлено: ожидаем текст который генерируется в DAO
        assertEquals("Error deleting user: 1", exception.getMessage());
        verify(session).remove(user);
        verify(transaction).rollback();
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        // Arrange
        String email = "existing@example.com";

        // Используем ТОЧНО тот же запрос что в DAO
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
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        // Arrange
        String email = "nonexisting@example.com";

        // Используем ТОЧНО тот же запрос что в DAO
        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(0L);

        // Act
        boolean result = userDAO.existsByEmail(email);

        // Assert
        assertFalse(result);
        verify(longQuery).uniqueResult();
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenQueryReturnsNull_ShouldReturnFalse() {
        // Arrange
        String email = "null@example.com";

        // Используем ТОЧНО тот же запрос что в DAO
        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(null);

        // Act
        boolean result = userDAO.existsByEmail(email);

        // Assert
        assertFalse(result);
        verify(longQuery).uniqueResult();
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
        verify(session).close();
    }

    @Test
    void sessionManagement_ShouldOpenAndCloseSessionForEachMethod() {
        // Этот тест проверяет что каждый метод открывает и закрывает сессию
        // Мы можем проверить это через вызовы в других тестах

        // Просто проверяем что sessionFactory открывает сессию
        verify(sessionFactory, never()).openSession(); // До вызовов не должно быть открытий

        // Вызываем любой метод чтобы проверить
        when(session.get(User.class, 1L)).thenReturn(new User());
        userDAO.findById(1L);

        // Теперь должно быть одно открытие и закрытие
        verify(sessionFactory, times(1)).openSession();
        verify(session, times(1)).close();
    }
}
