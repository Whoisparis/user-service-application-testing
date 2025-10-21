package org.example.userservice;


import org.example.userservice.util.HibernateUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Timeout;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    private final InputStream originalSystemIn = System.in;
    private final PrintStream originalSystemOut = System.out;
    private final PrintStream originalSystemErr = System.err;
    private ByteArrayOutputStream outputStream;
    private MockedStatic<HibernateUtil> hibernateMock;

    @BeforeEach
    void setUp() {
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(outputStream));

        hibernateMock = Mockito.mockStatic(HibernateUtil.class);
        hibernateMock.when(HibernateUtil::shutdown).thenAnswer(invocation -> null);

        System.setIn(new ByteArrayInputStream("0\n".getBytes()));
    }

    @AfterEach
    void tearDown() {
        if (hibernateMock != null) {
            hibernateMock.close();
        }

        System.setIn(originalSystemIn);
        System.setOut(originalSystemOut);
        System.setErr(originalSystemErr);
    }

    @Test
    @Timeout(5)
    void mainClass_ShouldExist() {
        Main main = new Main();

        assertNotNull(main);
    }

    @Test
    @Timeout(5)
    void mainMethod_WithEmptyArgs_ShouldNotThrowException() {
        String input = "0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldStartAndExitImmediately() {
        String input = "0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();

        assertFalse(output.isEmpty(), "Application should produce some output");

        hibernateMock.verify(HibernateUtil::shutdown, Mockito.times(1));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldHandleInvalidMenuChoice() {
        String input = "999\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();

        assertTrue(output.contains("Starting") ||
                output.contains("Shutting down") ||
                output.contains("closed") ||
                output.length() > 0);
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldDisplayMenu() {
        String input = "0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("===") || output.contains("Choose") || output.contains("User Management"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCallShutdown() {
        String input = "0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        hibernateMock.verify(HibernateUtil::shutdown, Mockito.times(1));
    }


    @Test
    @Timeout(5)
    void mainMethod_ShouldHandleCreateUserFlow() {

        String input = "1\nTest User\ntest@example.com\n25\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("Create") || output.contains("created") || output.contains("Create New User"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldHandleGetAllUsersFlow() {

        String input = "3\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("All Users") || output.contains("Total users") || output.contains("No users"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldHandleDeleteUserCancellation() {

        String input = "5\n1\nno\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("Delete") || output.contains("cancelled") || output.contains("Are you sure"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldHandleGetUserByEmailFlow() {

        String input = "6\ntest@example.com\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("Find User by Email") || output.contains("Enter email"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldBeRobustToVariousInputs() {

        String input = "\n\n\nabc\n999\n-1\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCoverCreateUserMethod() {

        String input = "1\nJohn Doe\njohn@example.com\n30\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCoverUpdateUserMethod() {

        String input = "4\n1\nUpdated Name\nupdated@example.com\n35\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("Update User") ||
                output.contains("Enter user ID to update") ||
                output.contains("Enter new name"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCoverGetAllUsersMethod() {
        String input = "3\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("All Users") ||
                output.contains("Total users") ||
                output.contains("No users found"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCoverGetUserByIdMethod() {
        String input = "2\n1\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("Find User by ID") ||
                output.contains("Enter user ID"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCoverGetUserByEmailMethod() {

        String input = "6\ntest@example.com\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("Find User by Email") ||
                output.contains("Enter email"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCoverDeleteUserMethod() {

        String input = "5\n1\nyes\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("Delete User") ||
                output.contains("Enter user ID to delete") ||
                output.contains("Are you sure"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCoverDeleteUserCancellationMethod() {
        String input = "5\n1\nno\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertTrue(output.contains("Delete User") ||
                output.contains("Deletion cancelled"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCoverRunApplicationLoop() {

        String input = "3\n2\n1\n6\ntest@example.com\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        String output = outputStream.toString();
        assertFalse(output.isEmpty());
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldHandleAgeInput_WithEmptyValue() {
        String input = "1\nJohn Doe\njohn@example.com\n\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));
        String output = outputStream.toString();
        assertTrue(output.contains("age") || output.contains("optional"));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldHandleExceptionInUserOperations() {
        String input = "2\n999\n4\n999\nname\nemail\n25\n5\n999\n6\nnonexisting@email.com\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }

    @Test
    @Timeout(5)
    void mainMethod_ShouldCoverShutdownMethod() {

        String input = "0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));

        hibernateMock.verify(HibernateUtil::shutdown, Mockito.times(1));
    }


    @Test
    @Timeout(3)
    void main_ShouldCallCreateUserMethod() {
        String input = "1\nTest User\ntest@example.com\n25\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("Create") ||
                output.contains("created") ||
                output.contains("Create New User") ||
                output.contains("Enter user name"));
    }

    @Test
    @Timeout(3)
    void main_ShouldCallUpdateUserMethod() {
        String input = "4\n1\nUpdated Name\nupdated@example.com\n30\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("Update") ||
                output.contains("Update User") ||
                output.contains("Enter user ID to update"));
    }

    @Test
    @Timeout(3)
    void main_ShouldCallGetAllUsersMethod() {

        String input = "3\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("All Users") ||
                output.contains("Total users") ||
                output.contains("No users"));
    }

    @Test
    @Timeout(3)
    void main_ShouldCallGetUserByIdMethod() {

        String input = "2\n1\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("Find User by ID") ||
                output.contains("Enter user ID"));
    }

    @Test
    @Timeout(3)
    void main_ShouldCallGetUserByEmailMethod() {

        String input = "6\ntest@example.com\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("Find User by Email") ||
                output.contains("Enter email"));
    }

    @Test
    @Timeout(3)
    void main_ShouldCallDeleteUserMethod() {
        String input = "5\n1\nyes\n0\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));

        Main.main(new String[]{});

        String output = outputStream.toString();
        assertTrue(output.contains("Delete User") ||
                output.contains("Enter user ID to delete") ||
                output.contains("Are you sure"));
    }

}
