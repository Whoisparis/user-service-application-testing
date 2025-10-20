package org.example.userservice.service;

import org.example.userservice.dao.UserDAO;
import org.example.userservice.entity.User;
import org.example.userservice.exception.EmailAlreadyExistsException;
import org.example.userservice.exception.UserNotFoundException;
import org.example.userservice.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDAO);
    }

    @Test
    void createUser_WithValidData_ShouldSaveUser() {
        String name = "John Doe";
        String email = "john@example.com";
        int age = 30;

        when(userDAO.existsByEmail(email)).thenReturn(false);
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        User result = userService.createUser(name, email, age);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(age, result.getAge());

        verify(userDAO).existsByEmail(email);
        verify(userDAO).save(any(User.class));
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowException() {
        String name = "John Doe";
        String email = "john@example.com";
        int age = 30;

        when(userDAO.existsByEmail(email)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(name, email, age);
        });

        verify(userDAO).existsByEmail(email);
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void createUser_WithInvalidEmail_ShouldThrowValidationException() {
        String name = "John Doe";
        String invalidEmail = "invalid-email";
        int age = 30;

        assertThrows(ValidationException.class, () -> {
            userService.createUser(name, invalidEmail, age);
        });

        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void createUser_WithInvalidAge_ShouldThrowValidationException() {
        String name = "John Doe";
        String email = "john@example.com";
        int invalidAge = -5;

        assertThrows(ValidationException.class, () -> {
            userService.createUser(name, email, invalidAge);
        });
    }

    @Test
    void createUser_WithEmptyName_ShouldThrowValidationException() {
        String emptyName = "";
        String email = "john@example.com";
        int age = 30;

        assertThrows(ValidationException.class, () -> {
            userService.createUser(emptyName, email, age);
        });
    }

    @Test
    void createUser_WithNullName_ShouldThrowValidationException() {
        String nullName = null;
        String email = "john@example.com";
        int age = 30;

        assertThrows(ValidationException.class, () -> {
            userService.createUser(nullName, email, age);
        });
    }

    @Test
    void getUserById_WhenUserExists_ShouldReturnUser() {
        Long userId = 1L;
        User expectedUser = new User();
        expectedUser.setId(userId);
        expectedUser.setName("John Doe");
        expectedUser.setEmail("john@example.com");
        expectedUser.setAge(30);

        when(userDAO.findById(userId)).thenReturn(Optional.of(expectedUser));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(expectedUser, result);
        verify(userDAO).findById(userId);
    }

    @Test
    void getUserById_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;
        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        verify(userDAO).findById(userId);
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setName("John Doe");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Smith");

        List<User> expectedUsers = Arrays.asList(user1, user2);
        when(userDAO.findAll()).thenReturn(expectedUsers);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedUsers, result);
        verify(userDAO).findAll();
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateUser() {
        Long userId = 1L;
        String newName = "Updated Name";
        String newEmail = "updated@example.com";
        int newAge = 35;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");
        existingUser.setAge(30);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.existsByEmail(newEmail)).thenReturn(false);
        when(userDAO.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, newName, newEmail, newAge);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals(newName, result.getName());
        assertEquals(newEmail, result.getEmail());
        assertEquals(newAge, result.getAge());

        verify(userDAO).findById(userId);
        verify(userDAO).existsByEmail(newEmail);
        verify(userDAO).update(any(User.class));
    }

    @Test
    void updateUser_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;
        String name = "Name";
        String email = "email@example.com";
        int age = 30;

        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(userId, name, email, age);
        });

        verify(userDAO).findById(userId);
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithExistingEmail_ShouldThrowException() {
        Long userId = 1L;
        String name = "John Doe";
        String existingEmail = "existing@example.com";
        int age = 30;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.existsByEmail(existingEmail)).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.updateUser(userId, name, existingEmail, age);
        });

        verify(userDAO).findById(userId);
        verify(userDAO).existsByEmail(existingEmail);
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithSameEmail_ShouldUpdateUser() {
        Long userId = 1L;
        String name = "Updated Name";
        String sameEmail = "same@example.com";
        int age = 35;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail(sameEmail);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, name, sameEmail, age);

        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(sameEmail, result.getEmail());

        verify(userDAO).findById(userId);
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO).update(any(User.class));
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        Long userId = 1L;
        User existingUser = new User();
        existingUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userDAO).delete(userId);

        userService.deleteUser(userId);
        verify(userDAO).findById(userId);
        verify(userDAO).delete(userId);
    }

    @Test
    void deleteUser_WhenUserNotExists_ShouldThrowException() {
        Long userId = 999L;
        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        verify(userDAO).findById(userId);
        verify(userDAO, never()).delete(anyLong());
    }

    @Test
    void updateUser_WithNewEmail_ShouldCheckExistence() {
        Long userId = 1L;
        String name = "Updated Name";
        String newEmail = "new@example.com";
        int age = 35;

        User existingUser = new User();
        existingUser.setId(userId);
        existingUser.setName("Old Name");
        existingUser.setEmail("old@example.com");

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.existsByEmail(newEmail)).thenReturn(false);
        when(userDAO.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, name, newEmail, age);
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(newEmail, result.getEmail());

        verify(userDAO).findById(userId);
        verify(userDAO).existsByEmail(newEmail);
        verify(userDAO).update(any(User.class));
    }
}
