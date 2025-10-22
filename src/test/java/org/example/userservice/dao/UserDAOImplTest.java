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
        lenient().when(sessionFactory.openSession()).thenReturn(session);
        lenient().when(session.beginTransaction()).thenReturn(transaction);
        userDAO = new UserDAOImpl(sessionFactory);
    }

    @Test
    void save_ShouldReturnSavedUser() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        doAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setId(1L);
            return null;
        }).when(session).persist(user);

        User savedUser = userDAO.save(user);

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
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        doThrow(new RuntimeException("Database error")).when(session).persist(user);

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
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAge(25);

        when(session.beginTransaction()).thenReturn(null);
        doThrow(new RuntimeException("Database error")).when(session).persist(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.save(user);
        });

        assertEquals("Error saving user: test@example.com", exception.getMessage());
        verify(session).persist(user);
        verify(session, never()).getTransaction();
        verify(session).close();
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUser() {
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setName("Test User");
        expectedUser.setEmail("test@example.com");
        expectedUser.setAge(25);

        when(session.get(User.class, userId)).thenReturn(expectedUser);

        Optional<User> result = userDAO.findById(userId);

        assertTrue(result.isPresent());
        assertEquals(expectedUser, result.get());

        verify(sessionFactory).openSession();
        verify(session).get(User.class, userId);
        verify(session, never()).beginTransaction();
        verify(session).close();
    }

    @Test
    void findById_WhenUserNotExists_ShouldReturnEmptyOptional() {
        Long userId = 999L;
        when(session.get(User.class, userId)).thenReturn(null);

        Optional<User> result = userDAO.findById(userId);

        assertFalse(result.isPresent());
        verify(session).get(User.class, userId);
        verify(session).close();
    }

    @Test
    void findById_WhenExceptionOccurs_ShouldThrowRuntimeException() {
        Long userId = 1L;
        when(session.get(User.class, userId)).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.findById(userId);
        });

        assertEquals("Error finding user by id: 1", exception.getMessage());
        verify(session).get(User.class, userId);
        verify(session).close();
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("User 1");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("User 2");

        List<User> expectedUsers = Arrays.asList(user1, user2);

        when(session.createQuery("FROM User", User.class)).thenReturn(userQuery);
        when(userQuery.list()).thenReturn(expectedUsers);

        List<User> result = userDAO.findAll();

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

        when(session.createQuery("FROM User", User.class)).thenReturn(userQuery);
        when(userQuery.list()).thenReturn(Collections.emptyList());

        List<User> result = userDAO.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userQuery).list();
        verify(session).close();
    }


    @Test
    void update_ShouldUpdateUser() {
        User user = new User();
        user.setId(1L);
        user.setName("Updated User");
        user.setEmail("updated@example.com");
        user.setAge(30);

        when(session.merge(user)).thenReturn(user);

        User updatedUser = userDAO.update(user);

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
        User user = new User();
        user.setId(1L);
        user.setName("Updated User");
        user.setEmail("updated@example.com");

        doThrow(new RuntimeException("Database error")).when(session).merge(user);

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
        User user = new User();
        user.setId(1L);
        user.setName("Updated User");
        user.setEmail("updated@example.com");

        when(session.beginTransaction()).thenReturn(null);
        doThrow(new RuntimeException("Database error")).when(session).merge(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.update(user);
        });

        assertEquals("Error updating user: updated@example.com", exception.getMessage());
        verify(session).merge(user);
        verify(session, never()).getTransaction();
        verify(session).close();
    }


    @Test
    void delete_ShouldDeleteUser() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        user.setName("Test User");

        when(session.get(User.class, userId)).thenReturn(user);

        userDAO.delete(userId);

        verify(sessionFactory).openSession();
        verify(session).beginTransaction();
        verify(session).get(User.class, userId);
        verify(session).remove(user);
        verify(transaction).commit();
        verify(session).close();
    }

    @Test
    void delete_WhenUserNotExists_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        when(session.get(User.class, userId)).thenReturn(null);

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
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(session.get(User.class, userId)).thenReturn(user);
        doThrow(new RuntimeException("Database error")).when(session).remove(user);

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
        Long userId = 1L;
        User user = new User();
        user.setId(userId);

        when(session.get(User.class, userId)).thenReturn(user);
        when(session.beginTransaction()).thenReturn(null);
        doThrow(new RuntimeException("Database error")).when(session).remove(user);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.delete(userId);
        });

        assertEquals("Error deleting user: 1", exception.getMessage());
        verify(session).remove(user);
        verify(session, never()).getTransaction();
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenEmailExists_ShouldReturnTrue() {
        String email = "existing@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(1L);

        boolean result = userDAO.existsByEmail(email);

        assertTrue(result);
        verify(sessionFactory).openSession();
        verify(session).createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class);
        verify(longQuery).setParameter("email", email);
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction();
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenEmailNotExists_ShouldReturnFalse() {
        String email = "nonexisting@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(0L);

        boolean result = userDAO.existsByEmail(email);

        assertFalse(result);
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction();
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenQueryReturnsNull_ShouldReturnFalse() {
        String email = "null@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(null);

        boolean result = userDAO.existsByEmail(email);

        assertFalse(result);
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction();
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenCountIsGreaterThanZero_ShouldReturnTrue() {
        String email = "test@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(5L);
        boolean result = userDAO.existsByEmail(email);

        assertTrue(result);
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction();
        verify(session).close();
    }



    @Test
    void existsByEmail_WhenExceptionOccurs_ShouldThrowRuntimeException() {
        String email = "error@example.com";

        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userDAO.existsByEmail(email);
        });

        assertEquals("Error checking if email exists: error@example.com", exception.getMessage());
        verify(longQuery).uniqueResult();
        verify(session, never()).beginTransaction();
        verify(session).close();
    }

    @Test
    void existsByEmail_WhenTransactionIsNull_ShouldHandleGracefully() {
        String email = "test@example.com";

        when(session.beginTransaction()).thenReturn(null);
        when(session.createQuery("SELECT COUNT(*) FROM User WHERE email = :email", Long.class))
                .thenReturn(longQuery);
        when(longQuery.setParameter("email", email)).thenReturn(longQuery);
        when(longQuery.uniqueResult()).thenReturn(1L);

        boolean result = userDAO.existsByEmail(email);

        assertTrue(result);
        verify(longQuery).uniqueResult();
        verify(session).close();
    }

    @Test
    void constructor_WithNullSessionFactory_ShouldWork() {
        assertDoesNotThrow(() -> new UserDAOImpl(null));
    }

    @Test
    void sessionManagement_ShouldOpenAndCloseSessionForEachMethod() {
        reset(sessionFactory, session);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.get(User.class, 1L)).thenReturn(new User());

        userDAO.findById(1L);

        verify(sessionFactory, times(1)).openSession();
        verify(session, times(1)).close();
    }

    @Test
    void multipleOperations_ShouldManageSessionsIndependently() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        when(session.get(User.class, 1L)).thenReturn(user);
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return null;
        }).when(session).persist(any(User.class));

        userDAO.findById(1L);
        userDAO.save(new User());
        userDAO.findById(1L);

        verify(sessionFactory, times(3)).openSession();
        verify(session, times(3)).close();
    }
}
