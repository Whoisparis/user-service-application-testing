package org.example.userservice;

import org.example.userservice.entity.User;
import org.example.userservice.exception.EmailAlreadyExistsException;
import org.example.userservice.exception.UserNotFoundException;
import org.example.userservice.exception.ValidationException;
import org.example.userservice.service.UserService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Scanner;

public class UserConsoleApp {
    private static final Logger logger = LogManager.getLogger(UserConsoleApp.class);
    private final UserService userService;
    private final Scanner scanner;
    private boolean running;

    public UserConsoleApp(UserService userService, Scanner scanner) {
        this.userService = userService;
        this.scanner = scanner;
        this.running = true;
    }

    public void run() {
        logger.info("Starting User Console application...");

        while (running) {
            printMenu();
            String choice = scanner.nextLine();

            try {
                processChoice(choice);
            } catch (UserNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (EmailAlreadyExistsException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (ValidationException e) {
                System.out.println("Validation error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
                logger.error("Unexpected error", e);
            }

            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
    }

    public void stop() {
        this.running = false;
    }

    void processChoice(String choice) {
        switch (choice) {
            case "1":
                createUser();
                break;
            case "2":
                getUserById();
                break;
            case "3":
                getAllUsers();
                break;
            case "4":
                updateUser();
                break;
            case "5":
                deleteUser();
                break;
            case "6":
                getUserByEmail();
                break;
            case "0":
                running = false;
                System.out.println("Application closed!");
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    void printMenu() {
        System.out.println("\n=== User Management System ===");
        System.out.println("1. Create User");
        System.out.println("2. Find User by ID");
        System.out.println("3. Find All Users");
        System.out.println("4. Update User");
        System.out.println("5. Delete User");
        System.out.println("6. Find User by Email");
        System.out.println("0. Exit");
        System.out.print("Choose an option: ");
    }

    public boolean isRunning() {
        return running;
    }

    void createUser() {
        System.out.println("\n--- Create New User ---");

        System.out.print("Enter user name: ");
        String name = scanner.nextLine();

        System.out.print("Enter user email: ");
        String email = scanner.nextLine();

        System.out.print("Enter age (optional, press Enter to skip): ");
        String ageInput = scanner.nextLine();
        Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

        User user = userService.createUser(name, email, age);
        System.out.println("User created successfully!");
        System.out.println("Created: " + user);
    }

    void getUserById() {
        System.out.println("\n--- Find User by ID ---");

        System.out.print("Enter user ID: ");
        Long id = Long.parseLong(scanner.nextLine());

        User user = userService.getUserById(id);
        System.out.println("User found:");
        System.out.println(user);
    }

    void getAllUsers() {
        System.out.println("\n--- All Users ---");

        List<User> users = userService.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("No users found.");
        } else {
            System.out.println("Total users: " + users.size());
            for (int i = 0; i < users.size(); i++) {
                System.out.println((i + 1) + ". " + users.get(i));
            }
        }
    }

    void updateUser() {
        System.out.println("\n--- Update User ---");

        System.out.print("Enter user ID to update: ");
        Long id = Long.parseLong(scanner.nextLine());

        User currentUser = userService.getUserById(id);
        System.out.println("Current user data: " + currentUser);

        System.out.print("Enter new name: ");
        String name = scanner.nextLine();

        System.out.print("Enter new email: ");
        String email = scanner.nextLine();

        System.out.print("Enter new age (optional, press Enter to skip): ");
        String ageInput = scanner.nextLine();
        Integer age = ageInput.isEmpty() ? null : Integer.parseInt(ageInput);

        User updatedUser = userService.updateUser(id, name, email, age);
        System.out.println("User updated successfully!");
        System.out.println("Updated: " + updatedUser);
    }

    void deleteUser() {
        System.out.println("\n--- Delete User ---");

        System.out.print("Enter user ID to delete: ");
        Long id = Long.parseLong(scanner.nextLine());

        User user = userService.getUserById(id);
        System.out.println("User to delete: " + user);

        System.out.print("Are you sure? (yes/no): ");
        String confirmation = scanner.nextLine();

        if ("yes".equalsIgnoreCase(confirmation)) {
            userService.deleteUser(id);
            System.out.println("User deleted successfully!");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    void getUserByEmail() {
        System.out.println("\n--- Find User by Email ---");

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        User user = userService.getUserByEmail(email);
        System.out.println("User found:");
        System.out.println(user);
    }
}
