package org.example.userservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    // ========== UserNotFoundException TESTS ==========

    @Test
    void testUserNotFoundException_WithId() {
        // Arrange
        Long userId = 1L;

        // Act
        UserNotFoundException exception = new UserNotFoundException(userId);

        // Assert
        assertEquals("User not found with id: 1", exception.getMessage());
    }

    @Test
    void testUserNotFoundException_WithMessage() {
        // Arrange
        String message = "Custom not found message";

        // Act
        UserNotFoundException exception = new UserNotFoundException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testUserNotFoundException_WithNullId() {
        // Act
        UserNotFoundException exception = new UserNotFoundException((Long) null);

        // Assert
        assertEquals("User not found with id: null", exception.getMessage());
    }

    @Test
    void testUserNotFoundException_WithZeroId() {
        // Act
        UserNotFoundException exception = new UserNotFoundException(0L);

        // Assert
        assertEquals("User not found with id: 0", exception.getMessage());
    }

    @Test
    void testUserNotFoundException_WithNegativeId() {
        // Act
        UserNotFoundException exception = new UserNotFoundException(-1L);

        // Assert
        assertEquals("User not found with id: -1", exception.getMessage());
    }

    @Test
    void testUserNotFoundException_Inheritance() {
        // Act
        UserNotFoundException exception = new UserNotFoundException(1L);

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ========== EmailAlreadyExistsException TESTS ==========

    @Test
    void testEmailAlreadyExistsException_WithEmail() {
        // Arrange
        String email = "test@example.com";

        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(email);

        // Assert
        assertEquals("Email already exists: test@example.com", exception.getMessage());
    }

    @Test
    void testEmailAlreadyExistsException_WithNullEmail() {
        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(null);

        // Assert
        assertEquals("Email already exists: null", exception.getMessage());
    }

    @Test
    void testEmailAlreadyExistsException_WithEmptyEmail() {
        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("");

        // Assert
        assertEquals("Email already exists: ", exception.getMessage());
    }

    @Test
    void testEmailAlreadyExistsException_WithWhitespaceEmail() {
        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("   ");

        // Assert
        assertEquals("Email already exists:    ", exception.getMessage());
    }

    @Test
    void testEmailAlreadyExistsException_Inheritance() {
        // Act
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("test@example.com");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ========== ValidationException TESTS ==========

    @Test
    void testValidationException_WithMessage() {
        // Arrange
        String message = "Validation failed";

        // Act
        ValidationException exception = new ValidationException(message);

        // Assert
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testValidationException_WithNullMessage() {
        // Act
        ValidationException exception = new ValidationException(null);

        // Assert
        assertNull(exception.getMessage());
    }

    @Test
    void testValidationException_WithEmptyMessage() {
        // Act
        ValidationException exception = new ValidationException("");

        // Assert
        assertEquals("", exception.getMessage());
    }

    @Test
    void testValidationException_WithWhitespaceMessage() {
        // Act
        ValidationException exception = new ValidationException("   ");

        // Assert
        assertEquals("   ", exception.getMessage());
    }

    @Test
    void testValidationException_Inheritance() {
        // Act
        ValidationException exception = new ValidationException("error");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ========== EXCEPTION BEHAVIOR TESTS ==========

    @Test
    void testExceptions_CanBeThrownAndCaught() {
        // Test UserNotFoundException
        assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException(1L);
        });

        // Test EmailAlreadyExistsException
        assertThrows(EmailAlreadyExistsException.class, () -> {
            throw new EmailAlreadyExistsException("test@example.com");
        });

        // Test ValidationException
        assertThrows(ValidationException.class, () -> {
            throw new ValidationException("Validation error");
        });
    }

    @Test
    void testExceptions_MessageFormatting() {
        // Test different ID formats
        UserNotFoundException exception1 = new UserNotFoundException(123L);
        assertEquals("User not found with id: 123", exception1.getMessage());

        UserNotFoundException exception2 = new UserNotFoundException(Long.MAX_VALUE);
        assertEquals("User not found with id: " + Long.MAX_VALUE, exception2.getMessage());

        // Test different email formats
        EmailAlreadyExistsException exception3 = new EmailAlreadyExistsException("user.name+tag@example.co.uk");
        assertEquals("Email already exists: user.name+tag@example.co.uk", exception3.getMessage());
    }
}
