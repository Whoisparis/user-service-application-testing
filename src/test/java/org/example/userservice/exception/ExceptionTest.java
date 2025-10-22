package org.example.userservice.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void testUserNotFoundException_WithId() {
        Long userId = 1L;
        UserNotFoundException exception = new UserNotFoundException(userId);

        assertEquals("User not found with id: 1", exception.getMessage());
    }

    @Test
    void testUserNotFoundException_WithMessage() {
        String message = "Custom not found message";

        UserNotFoundException exception = new UserNotFoundException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testUserNotFoundException_WithNullId() {
        UserNotFoundException exception = new UserNotFoundException((Long) null);

        assertEquals("User not found with id: null", exception.getMessage());
    }

    @Test
    void testUserNotFoundException_WithZeroId() {

        UserNotFoundException exception = new UserNotFoundException(0L);

        assertEquals("User not found with id: 0", exception.getMessage());
    }

    @Test
    void testUserNotFoundException_WithNegativeId() {
        UserNotFoundException exception = new UserNotFoundException(-1L);

        assertEquals("User not found with id: -1", exception.getMessage());
    }

    @Test
    void testUserNotFoundException_Inheritance() {
        UserNotFoundException exception = new UserNotFoundException(1L);

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testEmailAlreadyExistsException_WithEmail() {
        String email = "test@example.com";

        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(email);

        assertEquals("Email already exists: test@example.com", exception.getMessage());
    }

    @Test
    void testEmailAlreadyExistsException_WithNullEmail() {
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(null);

        assertEquals("Email already exists: null", exception.getMessage());
    }

    @Test
    void testEmailAlreadyExistsException_WithEmptyEmail() {

        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("");

        assertEquals("Email already exists: ", exception.getMessage());
    }

    @Test
    void testEmailAlreadyExistsException_WithWhitespaceEmail() {
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("   ");

        assertEquals("Email already exists:    ", exception.getMessage());
    }

    @Test
    void testEmailAlreadyExistsException_Inheritance() {

        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("test@example.com");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testValidationException_WithMessage() {
        String message = "Validation failed";

        ValidationException exception = new ValidationException(message);

        assertEquals(message, exception.getMessage());
    }

    @Test
    void testValidationException_WithNullMessage() {
        ValidationException exception = new ValidationException(null);

        assertNull(exception.getMessage());
    }

    @Test
    void testValidationException_WithEmptyMessage() {
        ValidationException exception = new ValidationException("");

        assertEquals("", exception.getMessage());
    }

    @Test
    void testValidationException_WithWhitespaceMessage() {
        ValidationException exception = new ValidationException("   ");

        assertEquals("   ", exception.getMessage());
    }

    @Test
    void testValidationException_Inheritance() {
        ValidationException exception = new ValidationException("error");

        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void testExceptions_CanBeThrownAndCaught() {
        assertThrows(UserNotFoundException.class, () -> {
            throw new UserNotFoundException(1L);
        });

        assertThrows(EmailAlreadyExistsException.class, () -> {
            throw new EmailAlreadyExistsException("test@example.com");
        });

        assertThrows(ValidationException.class, () -> {
            throw new ValidationException("Validation error");
        });
    }

    @Test
    void testExceptions_MessageFormatting() {
        UserNotFoundException exception1 = new UserNotFoundException(123L);
        assertEquals("User not found with id: 123", exception1.getMessage());

        UserNotFoundException exception2 = new UserNotFoundException(Long.MAX_VALUE);
        assertEquals("User not found with id: " + Long.MAX_VALUE, exception2.getMessage());

        EmailAlreadyExistsException exception3 = new EmailAlreadyExistsException("user.name+tag@example.co.uk");
        assertEquals("Email already exists: user.name+tag@example.co.uk", exception3.getMessage());
    }
}
