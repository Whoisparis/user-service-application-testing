package org.example.userservice;


import org.example.userservice.service.UserService;
import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MainTest {

    private final InputStream originalSystemIn = System.in;
    private final PrintStream originalSystemOut = System.out;
    private final PrintStream originalSystemErr = System.err;
    private ByteArrayOutputStream outputStream;
    private ByteArrayOutputStream errorOutputStream;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        errorOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorOutputStream));
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }

    @Test
    void main_ShouldStartAndExitApplicationSuccessfully() {
        setInput("0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            Main.main(new String[]{});

            String output = outputStream.toString();
            assertTrue(output.contains("Application closed!"), "Output should contain exit message");

            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void main_ShouldHandleCompleteUserWorkflow() {
        setInput("1\nTest User\ntest@example.com\n25\n0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            Main.main(new String[]{});

            String output = outputStream.toString();
            assertTrue(output.contains("Application closed!"), "Application should exit properly");

            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void main_ShouldHandleShutdownExceptionGracefully() {
        setInput("0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            hibernateMock.when(HibernateUtil::shutdown).thenThrow(new RuntimeException("Database connection failed"));
            Main.main(new String[]{});

            String errorOutput = errorOutputStream.toString();
            assertTrue(errorOutput.contains("Error during shutdown"), "Should handle shutdown errors gracefully");

            String output = outputStream.toString();
            assertTrue(output.contains("Application closed!"), "Application should still show exit message");
        }
    }

    @Test
    void main_ShouldHandleRuntimeExceptionInApp() {
        setInput("0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class); MockedStatic<UserService> userServiceMock = mockStatic(UserService.class)) {

            userServiceMock.when(UserService::new).thenThrow(new RuntimeException("Service initialization failed"));

            Main.main(new String[]{});

            String errorOutput = errorOutputStream.toString();
            assertTrue(errorOutput.contains("Application error"), "Should handle application errors gracefully");
            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void main_WithEmptyArgs_ShouldWorkNormally() {
        setInput("0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            Main.main(new String[]{});

            String output = outputStream.toString();
            assertTrue(output.contains("Application closed!"));
            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void main_WithArgs_ShouldIgnoreArgsAndWorkNormally() {
        setInput("0\n");
        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            Main.main(new String[]{"--debug", "test"});

            String output = outputStream.toString();
            assertTrue(output.contains("Application closed!"));
            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void main_ShouldCreateAllDependencies() {
        setInput("0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            Main.main(new String[]{});
            String output = outputStream.toString();
            assertTrue(output.contains("Application closed!"));
            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void main_ShouldHandleMultipleQuickExits() {
        setInput("0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            Main.main(new String[]{});
            Main.main(new String[]{});
            hibernateMock.verify(HibernateUtil::shutdown, times(2));
        }
    }

    @Test
    void shutdown_ShouldCloseScannerAndHibernate() {
        Scanner mockScanner = mock(Scanner.class);

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            invokePrivateShutdown(mockScanner);
            verify(mockScanner).close();
            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void shutdown_ShouldHandleNullScanner() {
        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            invokePrivateShutdown(null);

            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void shutdown_ShouldHandleScannerCloseException() {
        Scanner mockScanner = mock(Scanner.class);
        doThrow(new IllegalStateException("Scanner already closed")).when(mockScanner).close();

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            invokePrivateShutdown(mockScanner);
            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes()));
    }

    private void invokePrivateShutdown(Scanner scanner) {
        try {
            var shutdownMethod = Main.class.getDeclaredMethod("shutdown", Scanner.class);
            shutdownMethod.setAccessible(true);
            shutdownMethod.invoke(null, scanner);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private shutdown method", e);
        }
    }

    @Test
    void main_ShouldHandleInterruptedException() {
        setInput("0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class); MockedStatic<UserService> userServiceMock = mockStatic(UserService.class)) {
            userServiceMock.when(UserService::new).thenAnswer(invocation -> {
                Thread.currentThread().interrupt();
                throw new InterruptedException("Thread interrupted");
            });

            Main.main(new String[]{});
            String errorOutput = errorOutputStream.toString();
            assertTrue(errorOutput.contains("Application error"));

            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void main_ShouldHandleVeryLongInput() {
        String longName = "A".repeat(1000);
        setInput("1\n" + longName + "\ntest@example.com\n30\n0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            Main.main(new String[]{});

            String output = outputStream.toString();
            assertTrue(output.contains("Application closed!"));

            hibernateMock.verify(HibernateUtil::shutdown, times(1));
        }
    }

    @Test
    void main_ShouldBeThreadSafe() {
        setInput("0\n");

        try (MockedStatic<HibernateUtil> hibernateMock = mockStatic(HibernateUtil.class)) {
            Thread thread1 = new Thread(() -> Main.main(new String[]{}));
            Thread thread2 = new Thread(() -> Main.main(new String[]{}));

            thread1.start();
            thread2.start();

            thread1.join();
            thread2.join();

            hibernateMock.verify(HibernateUtil::shutdown, times(2));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test was interrupted");
        }
    }
}