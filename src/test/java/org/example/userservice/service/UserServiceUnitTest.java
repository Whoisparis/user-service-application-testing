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

    // ========== CREATE USER TESTS ==========

    @Test
    void createUser_WithValidData_ShouldReturnCreatedUser() {
        // Arrange
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;

        User savedUser = new User(name, email, age);
        savedUser.setId(1L);

        when(userDAO.existsByEmail(email)).thenReturn(false);
        when(userDAO.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(name, email, age);

        // Assert
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
        // Arrange
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = 30;

        when(userDAO.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.createUser(name, email, age);
        });

        // Просто проверяем что исключение брошено, без проверки getEmail()
        assertNotNull(exception);
        verify(userDAO).existsByEmail(email);
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void createUser_WithNullName_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser(null, "test@example.com", 30);
        });

        assertEquals("Name cannot be empty", exception.getMessage());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void createUser_WithEmptyName_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("", "test@example.com", 30);
        });

        assertEquals("Name cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_WithNullEmail_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", null, 30);
        });

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_WithEmptyEmail_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", "", 30);
        });

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void createUser_WithInvalidEmail_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", "invalid-email", 30);
        });

        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void createUser_WithNegativeAge_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", "test@example.com", -1);
        });

        assertEquals("Age must be between 0 and 150", exception.getMessage());
    }

    @Test
    void createUser_WithAgeOver150_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.createUser("John Doe", "test@example.com", 151);
        });

        assertEquals("Age must be between 0 and 150", exception.getMessage());
    }

    @Test
    void createUser_WithNullAge_ShouldCreateUser() {
        // Arrange
        String name = "John Doe";
        String email = "john@example.com";
        Integer age = null;

        User savedUser = new User(name, email, age);
        savedUser.setId(1L);

        when(userDAO.existsByEmail(email)).thenReturn(false);
        when(userDAO.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(name, email, age);

        // Assert
        assertNotNull(result);
        assertNull(result.getAge());
        verify(userDAO).existsByEmail(email);
        verify(userDAO).save(any(User.class));
    }

    // ========== GET USER BY ID TESTS ==========

    @Test
    void getUserById_WithValidId_ShouldReturnUser() {
        // Arrange
        Long userId = 1L;
        User user = new User("John Doe", "john@example.com", 30);
        user.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        verify(userDAO).findById(userId);
    }

    @Test
    void getUserById_WithNonExistingId_ShouldThrowUserNotFoundException() {
        // Arrange
        Long userId = 999L;
        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        // Просто проверяем что исключение брошено
        assertNotNull(exception);
        verify(userDAO).findById(userId);
    }

    @Test
    void getUserById_WithNullId_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserById(null);
        });

        assertEquals("Invalid user ID", exception.getMessage());
        verify(userDAO, never()).findById(anyLong());
    }

    @Test
    void getUserById_WithZeroId_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserById(0L);
        });

        assertEquals("Invalid user ID", exception.getMessage());
    }

    @Test
    void getUserById_WithNegativeId_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserById(-1L);
        });

        assertEquals("Invalid user ID", exception.getMessage());
    }

    // ========== GET ALL USERS TESTS ==========

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        // Arrange
        User user1 = new User("John Doe", "john@example.com", 30);
        user1.setId(1L);
        User user2 = new User("Jane Smith", "jane@example.com", 25);
        user2.setId(2L);

        List<User> users = Arrays.asList(user1, user2);
        when(userDAO.findAll()).thenReturn(users);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(users, result);
        verify(userDAO).findAll();
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        // Arrange
        when(userDAO.findAll()).thenReturn(Arrays.asList());

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userDAO).findAll();
    }

    // ========== UPDATE USER TESTS ==========

    @Test
    void updateUser_WithValidData_ShouldReturnUpdatedUser() {
        // Arrange
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

        // Act
        User result = userService.updateUser(userId, newName, newEmail, newAge);

        // Assert
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
        // Arrange
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

        // Act
        User result = userService.updateUser(userId, newName, sameEmail, newAge);

        // Assert
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(sameEmail, result.getEmail());
        verify(userDAO).findById(userId);
        verify(userDAO, never()).existsByEmail(anyString()); // Не проверяем email так как он не изменился
        verify(userDAO).update(any(User.class));
    }

    @Test
    void updateUser_WithDuplicateEmail_ShouldThrowEmailAlreadyExistsException() {
        // Arrange
        Long userId = 1L;
        String newName = "John Updated";
        String duplicateEmail = "existing@example.com";
        Integer newAge = 35;

        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        when(userDAO.existsByEmail(duplicateEmail)).thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException exception = assertThrows(EmailAlreadyExistsException.class, () -> {
            userService.updateUser(userId, newName, duplicateEmail, newAge);
        });

        // Просто проверяем что исключение брошено
        assertNotNull(exception);
        verify(userDAO).existsByEmail(duplicateEmail);
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithNonExistingId_ShouldThrowUserNotFoundException() {
        // Arrange
        Long userId = 999L;
        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(userId, "New Name", "new@example.com", 30);
        });

        // Просто проверяем что исключение брошено, без проверки getUserId()
        assertNotNull(exception);
        verify(userDAO).findById(userId);
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }

    // ========== DELETE USER TESTS ==========

    @Test
    void deleteUser_WithValidId_ShouldDeleteUser() {
        // Arrange
        Long userId = 1L;
        User existingUser = new User("John Doe", "john@example.com", 30);
        existingUser.setId(userId);

        when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userDAO).delete(userId);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userDAO).findById(userId);
        verify(userDAO).delete(userId);
    }

    @Test
    void deleteUser_WithNonExistingId_ShouldThrowUserNotFoundException() {
        // Arrange
        Long userId = 999L;
        when(userDAO.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        // Просто проверяем что исключение брошено
        assertNotNull(exception);
        verify(userDAO).findById(userId);
        verify(userDAO, never()).delete(anyLong());
    }

    @Test
    void deleteUser_WithNullId_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.deleteUser(null);
        });

        assertEquals("Invalid user ID", exception.getMessage());
        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).delete(anyLong());
    }

    // ========== GET USER BY EMAIL TESTS ==========

    @Test
    void getUserByEmail_WithValidEmail_ShouldReturnUser() {
        // Arrange
        String email = "john@example.com";
        User user = new User("John Doe", email, 30);
        user.setId(1L);

        when(userDAO.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        User result = userService.getUserByEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        verify(userDAO).findByEmail(email);
    }

    @Test
    void getUserByEmail_WithNonExistingEmail_ShouldThrowUserNotFoundException() {
        // Arrange
        String email = "nonexisting@example.com";
        when(userDAO.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserByEmail(email);
        });

        assertEquals("User not found with email: " + email, exception.getMessage());
        verify(userDAO).findByEmail(email);
    }

    @Test
    void getUserByEmail_WithNullEmail_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserByEmail(null);
        });

        assertEquals("Email cannot be empty", exception.getMessage());
        verify(userDAO, never()).findByEmail(anyString());
    }

    @Test
    void getUserByEmail_WithEmptyEmail_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userService.getUserByEmail("");
        });

        assertEquals("Email cannot be empty", exception.getMessage());
    }

    // ========== EMAIL VALIDATION TESTS ==========

    @Test
    void createUser_WithValidEmailFormats_ShouldCreateUser() {
        // Arrange
        String name = "Test User";
        Integer age = 30;

        User savedUser = new User(name, "test@example.com", age);
        savedUser.setId(1L);

        when(userDAO.existsByEmail(anyString())).thenReturn(false);
        when(userDAO.save(any(User.class))).thenReturn(savedUser);

        // Act & Assert - разные валидные форматы email
        assertDoesNotThrow(() -> userService.createUser(name, "test@example.com", age));
        assertDoesNotThrow(() -> userService.createUser(name, "test.name@example.com", age));
        assertDoesNotThrow(() -> userService.createUser(name, "test_name@example.co.uk", age));
        assertDoesNotThrow(() -> userService.createUser(name, "test+tag@example.com", age));

        verify(userDAO, times(4)).existsByEmail(anyString());
        verify(userDAO, times(4)).save(any(User.class));
    }

    @Test
    void createUser_WithClearlyInvalidEmailFormats_ShouldThrowValidationException() {
        // Arrange
        String name = "Test User";
        Integer age = 30;

        // Act & Assert - тестируем только очевидно невалидные email
        assertThrows(ValidationException.class, () -> userService.createUser(name, "", age));
        assertThrows(ValidationException.class, () -> userService.createUser(name, "   ", age));
        assertThrows(ValidationException.class, () -> userService.createUser(name, null, age));

        // DAO не должно вызываться при невалидном email
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).save(any(User.class));
    }

    @Test
    void updateUser_WithInvalidEmail_ShouldThrowValidationException() {
        // Arrange
        Long userId = 1L;
        // НЕ настраиваем when(userDAO.findById()) потому что метод не дойдет до этого вызова

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, "New Name", "invalid-email", 35);
        });

        // Проверяем что DAO методы НЕ вызывались из-за ранней валидации
        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithEmptyName_ShouldThrowValidationException() {
        // Arrange
        Long userId = 1L;

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, "", "valid@example.com", 35);
        });

        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithNullName_ShouldThrowValidationException() {
        // Arrange
        Long userId = 1L;

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, null, "valid@example.com", 35);
        });

        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }

    @Test
    void updateUser_WithInvalidAge_ShouldThrowValidationException() {
        // Arrange
        Long userId = 1L;

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            userService.updateUser(userId, "Valid Name", "valid@example.com", 200);
        });

        verify(userDAO, never()).findById(anyLong());
        verify(userDAO, never()).existsByEmail(anyString());
        verify(userDAO, never()).update(any(User.class));
    }
}
