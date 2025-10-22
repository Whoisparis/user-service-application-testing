package org.example.userservice;


import org.example.userservice.service.UserService;
import org.example.userservice.util.HibernateUtil;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        Scanner scanner = new Scanner(System.in);

        UserConsoleApp app = new UserConsoleApp(userService, scanner);

        try {
            app.run();
        } catch (Exception e) {
            System.err.println("Application error: " + e.getMessage());
        } finally {
            shutdown(scanner);
        }
    }

    private static void shutdown(Scanner scanner) {
        try {
            HibernateUtil.shutdown();
        } catch (Exception e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
}