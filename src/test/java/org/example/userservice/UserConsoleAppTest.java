package org.example.userservice;

import org.example.userservice.entity.User;
import org.example.userservice.exception.EmailAlreadyExistsException;
import org.example.userservice.exception.UserNotFoundException;
import org.example.userservice.exception.ValidationException;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserConsoleAppTest {

    @Mock
    private UserService userService;

    private final InputStream originalSystemIn = System.in;
    private final PrintStream originalSystemOut = System.out;
    private ByteArrayOutputStream outputStream;
    private UserConsoleApp app;
    private Scanner scanner;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
        if (scanner != null) {
            scanner.close();
        }
    }

    @Test
    void printMenu_ShouldDisplayAllOptions() {
        setInput("");
        app = new UserConsoleApp(userService, scanner);

        app.printMenu();

        String output = outputStream.toString();
        assertTrue(output.contains("User Management System"));
        assertTrue(output.contains("1. Create User"));
        assertTrue(output.contains("2. Find User by ID"));
        assertTrue(output.contains("3. Find All Users"));
        assertTrue(output.contains("4. Update User"));
        assertTrue(output.contains("5. Delete User"));
        assertTrue(output.contains("6. Find User by Email"));
        assertTrue(output.contains("0. Exit"));
    }

    @Test
    void processChoice_CreateUser_ShouldCallCreateUser() {

        setInput("");
        app = new UserConsoleApp(userService, scanner);

        app.processChoice("1");

        String output = outputStream.toString();
        assertTrue(output.contains("Create New User"));
    }

    @Test
    void processChoice_GetUserById_ShouldCallGetUserById() {
        setInput("");
        app = new UserConsoleApp(userService, scanner);

        app.processChoice("2");

        String output = outputStream.toString();
        assertTrue(output.contains("Find User by ID"));
    }

    @Test
    void processChoice_GetAllUsers_ShouldCallGetAllUsers() {
        setInput("");
        app = new UserConsoleApp(userService, scanner);

        app.processChoice("3");

        String output = outputStream.toString();
        assertTrue(output.contains("All Users"));
    }

    @Test
    void processChoice_UpdateUser_ShouldCallUpdateUser() {
        setInput("");
        app = new UserConsoleApp(userService, scanner);

        app.processChoice("4");

        String output = outputStream.toString();
        assertTrue(output.contains("Update User"));
    }

    @Test
    void processChoice_DeleteUser_ShouldCallDeleteUser() {
        setInput("");
        app = new UserConsoleApp(userService, scanner);
        app.processChoice("5");
        String output = outputStream.toString();
        assertTrue(output.contains("Delete User"));
    }

    @Test
    void processChoice_GetUserByEmail_ShouldCallGetUserByEmail() {
        setInput("");
        app = new UserConsoleApp(userService, scanner);
        app.processChoice("6");
        String output = outputStream.toString();
        assertTrue(output.contains("Find User by Email"));
    }

    @Test
    void processChoice_Exit_ShouldStopApplication() {
        setInput("");
        app = new UserConsoleApp(userService, scanner);

        app.processChoice("0");

        String output = outputStream.toString();
        assertTrue(output.contains("Application closed!"));
        assertFalse(app.isRunning());
    }

    @Test
    void processChoice_InvalidChoice_ShouldShowErrorMessage() {
        setInput("");
        app = new UserConsoleApp(userService, scanner);
        app.processChoice("999");

        String output = outputStream.toString();
        assertTrue(output.contains("Invalid choice"));
    }

    @Test
    void createUser_ShouldCreateUserSuccessfully() {
        User mockUser = createMockUser(1L, "Test User", "test@test.com", 25);
        when(userService.createUser("Test User", "test@test.com", 25)).thenReturn(mockUser);
        setInput("Test User\ntest@test.com\n25\n");
        app = new UserConsoleApp(userService, scanner);

        app.createUser();

        String output = outputStream.toString();
        assertTrue(output.contains("User created successfully"));
        verify(userService).createUser("Test User", "test@test.com", 25);
    }

    @Test
    void createUser_WithOptionalAge_ShouldCreateUser() {
        User mockUser = createMockUser(1L, "Test User", "test@test.com", null);
        when(userService.createUser("Test User", "test@test.com", null)).thenReturn(mockUser);
        setInput("Test User\ntest@test.com\n\n");
        app = new UserConsoleApp(userService, scanner);

        app.createUser();

        verify(userService).createUser("Test User", "test@test.com", null);
    }

    @Test
    void getUserById_ShouldFindUser() {
        User mockUser = createMockUser(1L, "Test User", "test@test.com", 25);
        when(userService.getUserById(1L)).thenReturn(mockUser);
        setInput("1\n");
        app = new UserConsoleApp(userService, scanner);
        app.getUserById();

        String output = outputStream.toString();
        assertTrue(output.contains("User found"));
        verify(userService).getUserById(1L);
    }

    @Test
    void getAllUsers_ShouldDisplayUsers() {
        List<User> users = Arrays.asList(
                createMockUser(1L, "User 1", "user1@test.com", 25),
                createMockUser(2L, "User 2", "user2@test.com", 30)
        );
        when(userService.getAllUsers()).thenReturn(users);
        setInput("");
        app = new UserConsoleApp(userService, scanner);

        app.getAllUsers();

        String output = outputStream.toString();
        assertTrue(output.contains("Total users: 2"));
        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_EmptyList_ShouldDisplayNoUsers() {
        when(userService.getAllUsers()).thenReturn(Arrays.asList());
        setInput("");
        app = new UserConsoleApp(userService, scanner);

        app.getAllUsers();

        String output = outputStream.toString();
        assertTrue(output.contains("No users found"));
        verify(userService).getAllUsers();
    }

    @Test
    void updateUser_ShouldUpdateSuccessfully() {

        User currentUser = createMockUser(1L, "Old Name", "old@test.com", 20);
        User updatedUser = createMockUser(1L, "New Name", "new@test.com", 30);

        when(userService.getUserById(1L)).thenReturn(currentUser);
        when(userService.updateUser(eq(1L), eq("New Name"), eq("new@test.com"), eq(30))).thenReturn(updatedUser);

        setInput("1\nNew Name\nnew@test.com\n30\n");
        app = new UserConsoleApp(userService, scanner);

        app.updateUser();

        String output = outputStream.toString();
        assertTrue(output.contains("User updated successfully"));
        verify(userService).updateUser(1L, "New Name", "new@test.com", 30);
    }

    @Test
    void deleteUser_WithConfirmation_ShouldDelete() {
        User mockUser = createMockUser(1L, "Test User", "test@test.com", 25);
        when(userService.getUserById(1L)).thenReturn(mockUser);

        setInput("1\nyes\n");
        app = new UserConsoleApp(userService, scanner);

        app.deleteUser();

        String output = outputStream.toString();
        assertTrue(output.contains("User deleted successfully"));
        verify(userService).deleteUser(1L);
    }

    @Test
    void deleteUser_WithCancellation_ShouldNotDelete() {
        User mockUser = createMockUser(1L, "Test User", "test@test.com", 25);
        when(userService.getUserById(1L)).thenReturn(mockUser);

        setInput("1\nno\n");
        app = new UserConsoleApp(userService, scanner);

        app.deleteUser();

        String output = outputStream.toString();
        assertTrue(output.contains("Deletion cancelled"));
        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    void getUserByEmail_ShouldFindUser() {

        User mockUser = createMockUser(1L, "Test User", "test@test.com", 25);
        when(userService.getUserByEmail("test@test.com")).thenReturn(mockUser);

        setInput("test@test.com\n");
        app = new UserConsoleApp(userService, scanner);

        app.getUserByEmail();

        verify(userService).getUserByEmail("test@test.com");
    }

    @Test
    void getUserById_WithUserNotFound_ShouldHandleException() {
        when(userService.getUserById(999L)).thenThrow(new UserNotFoundException(999L));
        setInput("999\n");
        app = new UserConsoleApp(userService, scanner);

        app.getUserById();

        String output = outputStream.toString();
        assertTrue(output.contains("Error"));
    }

    @Test
    void createUser_WithEmailExists_ShouldHandleException() {
        when(userService.createUser(anyString(), anyString(), any()))
                .thenThrow(new EmailAlreadyExistsException("Email exists"));
        setInput("Test\ntest@test.com\n25\n");
        app = new UserConsoleApp(userService, scanner);

        app.createUser();

        String output = outputStream.toString();
        assertTrue(output.contains("Error"));
    }

    @Test
    void createUser_WithValidationError_ShouldHandleException() {
        when(userService.createUser(anyString(), anyString(), any()))
                .thenThrow(new ValidationException("Invalid input"));
        setInput("Test\ntest@test.com\n25\n");
        app = new UserConsoleApp(userService, scanner);

        app.createUser();

        String output = outputStream.toString();
        assertTrue(output.contains("Validation error"));
    }

    @Test
    void stop_ShouldSetRunningToFalse() {
        setInput("");
        app = new UserConsoleApp(userService, scanner);

        app.stop();

        assertFalse(app.isRunning());
    }

    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        scanner = new Scanner(System.in);
    }

    private User createMockUser(Long id, String name, String email, Integer age) {
        User user = mock(User.class);
        when(user.getId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(user.getEmail()).thenReturn(email);
        when(user.getAge()).thenReturn(age);
        when(user.toString()).thenReturn(String.format("User{id=%d, name='%s', email='%s', age=%s}",
                id, name, email, age != null ? age : "null"));
        return user;
    }
}