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
    void createUser_WithValidData_ShouldReturnCreatedUser() {
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;

        User savedUser = new User(name, email, age);
        savedUser.setId(1L);

        when(userDAO.existsByEmail(email)).thenReturn(false);
        when(userDAO.save(any(User.class))).thenReturn(savedUser);

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
    void createUser_WithDuplicateEmail_ShouldThrowEmailAlreadyExistsException() {
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;

        when(userDAO.existsByEmail(email)).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(name, email, age);
        });

        assertNotNull(exception);
        verify(userDAO).existsByEmail(email);
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void createUser_WithNullName_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser(null, "test@example.com", 30);
        });

        assertEquals("Name cannot be empty", exception.getMessage());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void createUser_WithEmptyName_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("", "test@example.com", 30);
        });

        assertEquals("Name cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_WithNullEmail_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", null, 30);
        });

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_WithEmptyEmail_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", "", 30);
        });

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_WithInvalidEmail_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", "invalid-email", 30);
        });

        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void createUser_WithNegativeAge_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", "test@example.com", -1);
        });

        assertEquals("Age must be between 0 and 150", exception.getMessage());
    }

    @Test
    void createUser_WithAgeOver150_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", "test@example.com", 151);
        });

        assertEquals("Age must be between 0 and 150", exception.getMessage());
    }

    @Test
    void createUser_WithNullAge_ShouldCreateUser() {
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = null;

        User savedUser = new User(name, email, age);
        savedUser.setId(1L);

        when(userDAO.existsByEmail(email)).thenReturn(false);
        when(userDAO.save(any(User.class))).thenReturn(savedUser);

        User result = userService.createUser(name, email, age);
        assertNotNull(result);
        assertNull(result.getAge());
        verify(userDAO).existsByEmail(email);
        verify(userDAO).save(any(User.class));
    }

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        Long userId = 1L;
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userDAO).findById(userId);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldThrowUserNotFoundException() {

        Long userId = 999L;
        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        assertNotNull(exception);
        verify(userDAO).findById(userId);
    }

    @Test
    void getUserById_WithNullId_ShouldThrowValidationException() {

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserById(null);
        });

        assertEquals("Invalid user ID", exception.getMessage());
        verify(userDAO, never()).findById(anyLong());
    }

    @Test
    void getUserById_WithZeroId_ShouldThrowValidationException() {

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserById(0L);
        });

        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void getUserById_WithNegativeId_ShouldThrowValidationException() {

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserById(-1L);
        });

        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {

        User user1 = new User("John Doe", "john@example.com", 30);
        user1.setId(1L);
        User user2 = new User("Jane Smith", "jane@example.com", 25);
        user2.setId(2L);

        List<User> users = Arrays.asList(user1, user2);
        when(userDAO.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(users, result);
        verify(userDAO).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {

        when(userDAO.findAll()).thenReturn(Arrays.asList());

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userDAO).findAll();
    }

    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUser() {
        Long userId = 1L;
        String newName = "John Updated";
        String newEmail = "john.updated@example.com";
        Integer newAge = 35;

        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(userId);

        User updatedUser = new User(newName, newEmail, newAge);
        updatedUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.existsByEmail(newEmail)).thenReturn(false);
        when(userDAO.update(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(userId, newName, newEmail, newAge);

        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(newEmail, result.getEmail());
        assertEquals(newAge, result.getAge());
        verify(userDAO).findById(userId);
        verify(userDAO).existsByEmail(newEmail);
        verify(userDAO).update(any(User.class));
    }

    @Test
    void updateUser_WithSameEmail_ShouldUpdateUser() {
        Long userId = 1L;
        String newName = "John Updated";
        String sameEmail = "john@example.com";
        Integer newAge = 35;

        User existingUser = new User("John Doe", sameEmail, 30);
        existingUser.setId(userId);

        User updatedUser = new User(newName, sameEmail, newAge);
        updatedUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.update(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(userId, newName, sameEmail, newAge);

        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(sameEmail, result.getEmail());
        verify(userDAO).findById(userId);
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO).update(any(User.class));
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowEmailAlreadyExistsException() {
        Long userId = 1L;
        String newName = "John Updated";
        String duplicateEmail = "existing@example.com";
        Integer newAge = 35;

        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.existsByEmail(duplicateEmail)).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.updateUser(userId, newName, duplicateEmail, newAge);
        });

        assertNotNull(exception);
        verify(userDAO).existsByEmail(duplicateEmail);
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithNonExistingId_ShouldThrowUserNotFoundException() {

        Long userId = 999L;
        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(userId, "New Name", "new@example.com", 30);
        });

        assertNotNull(exception);
        verify(userDAO).findById(userId);
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        Long userId = 1L;
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userDAO).delete(userId);

        userService.deleteUser(userId);

        verify(userDAO).findById(userId);
        verify(userDAO).delete(userId);
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldThrowUserNotFoundException() {
        Long userId = 999L;
        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        assertNotNull(exception);
        verify(userDAO).findById(userId);
        verify(userDAO, never()).delete(anyLong());
    }

    @Test
    void deleteUser_WithNullId_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.deleteUser(null);
        });

        assertEquals("Invalid user ID", exception.getMessage());
        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).delete(anyLong());
    }

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() {

        String email = "john@example.com";
        User user = new User("John Doe", email, 30);
        user.setId(1L);

        when(userDAO.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userDAO).findByEmail(email);
    }

    @Test
    void getUserByEmail_WithNonExistingEmail_ShouldThrowUserNotFoundException() {
        String email = "nonexisting@example.com";
        when(userDAO.findByEmail(email)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByEmail(email);
        });

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userDAO).findByEmail(email);
    }

    @Test
    void getUserByEmail_WithNullEmail_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserByEmail(null);
        });

        assertEquals("Email cannot be empty", exception.getMessage());
        verify(userDAO, never()).findByEmail(anyString());
    }

    @Test
    void getUserByEmail_WithEmptyEmail_ShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserByEmail("");
        });

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_WithValidEmailFormats_ShouldCreateUser() {
        String name = "Test User";
        Integer age = 30;

        User savedUser = new User(name, "test@example.com", age);
        savedUser.setId(1L);

        when(userDAO.existsByEmail(anyString())).thenReturn(false);
        when(userDAO.save(any(User.class))).thenReturn(savedUser);
        assertDoesNotThrow(() -> userService.createUser(name, "test@example.com", age));
        assertDoesNotThrow(() -> userService.createUser(name, "test.name@example.com", age));
        assertDoesNotThrow(() -> userService.createUser(name, "test_name@example.co.uk", age));
        assertDoesNotThrow(() -> userService.createUser(name, "test+tag@example.com", age));

        verify(userDAO, times(4)).existsByEmail(anyString());
        verify(userDAO, times(4)).save(any(User.class));
    }

    @Test
    void createUser_WithClearlyInvalidEmailFormats_ShouldThrowValidationException() {

        String name = "Test User";
        Integer age = 30;

        assertThrows(ValidationException.class, () -> userService.createUser(name, "", age));
        assertThrows(ValidationException.class, () -> userService.createUser(name, "   ", age));
        assertThrows(ValidationException.class, () -> userService.createUser(name, null, age));

        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithInvalidEmail_ShouldThrowValidationException() {
        Long userId = 1L;
        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, "New Name", "invalid-email", 35);
        });

        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithEmptyName_ShouldThrowValidationException() {
        Long userId = 1L;

        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, "", "valid@example.com", 35);
        });

        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithNullName_ShouldThrowValidationException() {
        Long userId = 1L;

        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, null, "valid@example.com", 35);
        });

        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithInvalidAge_ShouldThrowValidationException() {

        Long userId = 1L;

        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, "Valid Name", "valid@example.com", 200);
        });

        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }
}
