package org.example.userservice.service;

import org.example.userservice.dao.UserDAO;
import org.example.userservice.dao.UserDAOImpl;
import org.example.userservice.entity.User;
import org.example.userservice.exception.EmailAlreadyExistsException;
import org.example.userservice.exception.UserNotFoundException;
import org.example.userservice.exception.ValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;


public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
    }

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User createUser(String name, String email, Integer age) {
        logger.info("Creating new user: {}", email);
        validateUserData(name, email, age);

        if (userDAO.existsByEmail(email)) {
            logger.warn("Email already exists: {}", email);
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User(name, email, age);
        return userDAO.save(user);
    }

    public User getUserById(Long id) {
        logger.info("Getting user by id: {}", id);
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid user ID");
        }

        return userDAO.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<User> getAllUsers() {
        logger.info("Getting all users");
        return userDAO.findAll();
    }

    public User updateUser(Long id, String name, String email, Integer age) {
        logger.info("Updating user with id: {}", id);
        validateUserData(name, email, age);

        User existingUser = getUserById(id);

        if (!existingUser.getEmail().equals(email) && userDAO.existsByEmail(email)) {
            logger.warn("Email already exists during update: {}", email);
            throw new EmailAlreadyExistsException(email);
        }

        existingUser.setName(name);
        existingUser.setEmail(email);
        existingUser.setAge(age);

        return userDAO.update(existingUser);
    }

    public void deleteUser(Long id) {
        logger.info("Deleting user with id: {}", id);
        if (id == null || id <= 0) {
            throw new ValidationException("Invalid user ID");
        }

        getUserById(id);
        userDAO.delete(id);
    }

    public User getUserByEmail(String email) {
        logger.info("Getting user by email: {}", email);
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }

        return userDAO.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
    }

    private void validateUserData(String name, String email, Integer age) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name cannot be empty");
        }

        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }

        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid email format");
        }

        if (age != null && (age < 0 || age > 150)) {
            throw new ValidationException("Age must be between 0 and 150");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}