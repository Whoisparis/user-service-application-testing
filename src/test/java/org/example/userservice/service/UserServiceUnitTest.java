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
public class UserServiceUnitTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDAO);
    }

    @Test
    void testCreateUser_WithValidData_ShouldCreateUser() {

        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;
        User expectedUser = new User(name, email, age);
        expectedUser.setId(1L);

        when(userDAO.existsByEmail(email)).thenReturn(false);
        when(userDAO.save(any(User.class))).thenReturn(expectedUser);

        User createdUser = userService.createUser(name, email, age);

        assertNotNull(createdUser);
        assertEquals(name, createdUser.getName());
        assertEquals(email, createdUser.getEmail());
        assertEquals(age, createdUser.getAge());

        verify(userDAO).existsByEmail(email);
        verify(userDAO).save(any(User.class));
    }

    @Test
    void testCreateUser_WithDuplicateEmail_ShouldThrowException() {

        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;

        when(userDAO.existsByEmail(email)).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.createUser(name, email, age));

        assertEquals("Email already exists: john@example.com", exception.getMessage());

        verify(userDAO).existsByEmail(email);
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_WhenUserExists_ShouldReturnUser() {

        Long userId = 1L;
        User expectedUser = new User("John Doe", "john@example.com", 30);
        expectedUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(expectedUser));


        User foundUser = userService.getUserById(userId);


        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
        assertEquals("John Doe", foundUser.getName());

        verify(userDAO).findById(userId);
    }

    @Test
    void testGetUserById_WhenUserNotExists_ShouldThrowException() {

        Long userId = 999L;

        when(userDAO.findById(userId)).thenReturn(Optional.empty());


        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(userId));

        verify(userDAO).findById(userId);
    }

    @Test
    void testGetAllUsers_ShouldReturnAllUsers() {

        User user1 = new User("User1", "user1@example.com", 25);
        User user2 = new User("User2", "user2@example.com", 30);
        List<User> expectedUsers = Arrays.asList(user1, user2);

        when(userDAO.findAll()).thenReturn(expectedUsers);


        List<User> users = userService.getAllUsers();


        assertEquals(2, users.size());
        verify(userDAO).findAll();
    }

    @Test
    void testUpdateUser_WithValidData_ShouldUpdateUser() {

        Long userId = 1L;
        User existingUser = new User("Old Name", "old@example.com", 25);
        existingUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.existsByEmail("new@example.com")).thenReturn(false);
        when(userDAO.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updateUser(userId, "New Name", "new@example.com", 30);

        assertEquals("New Name", updatedUser.getName());
        assertEquals("new@example.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());

        verify(userDAO).findById(userId);
        verify(userDAO).existsByEmail("new@example.com");
        verify(userDAO).update(existingUser);
    }

    @Test
    void testDeleteUser_WhenUserExists_ShouldDeleteUser() {

        Long userId = 1L;
        User existingUser = new User("To Delete", "delete@example.com", 25);
        existingUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userDAO).delete(userId);

        userService.deleteUser(userId);

        verify(userDAO).findById(userId);
        verify(userDAO).delete(userId);
    }

    @Test
    void testValidation_EmptyName_ShouldThrowException() {
        assertThrows(ValidationException.class,
                () -> userService.createUser("", "test@example.com", 25));
    }

    @Test
    void testValidation_EmptyEmail_ShouldThrowException() {
        assertThrows(ValidationException.class,
                () -> userService.createUser("John Doe", "", 25));
    }

    @Test
    void testValidation_InvalidEmail_ShouldThrowException() {
        assertThrows(ValidationException.class,
                () -> userService.createUser("John Doe", "invalid-email", 25));
    }

    @Test
    void testValidation_InvalidAge_ShouldThrowException() {
        assertThrows(ValidationException.class,
                () -> userService.createUser("John Doe", "test@example.com", -5));
    }

    @Test
    void testValidation_NullId_ShouldThrowException() {
        assertThrows(ValidationException.class,
                () -> userService.getUserById(null));
    }

    @Test
    void testValidation_InvalidId_ShouldThrowException() {
        assertThrows(ValidationException.class,
                () -> userService.getUserById(-1L));
    }
}
