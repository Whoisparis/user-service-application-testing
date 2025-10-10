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
    void testEmailAlreadyExistsException() {
        String email = "test@example.com";

        EmailAlreadyExistsException exception = new EmailAlreadyExistsException(email);

        assertEquals("Email already exists: test@example.com", exception.getMessage());
    }

    @Test
    void testValidationException() {
        String message = "Validation failed";

        ValidationException exception = new ValidationException(message);

        assertEquals(message, exception.getMessage());
    }
}
