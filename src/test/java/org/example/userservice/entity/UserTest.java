package org.example.userservice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    @Test
    void userConstructor_WithParameters_ShouldSetFields() {
        User user = new User("John", "john@test.com", 25);

        assertEquals("John", user.getName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals(25, user.getAge());
        assertNull(user.getId());
    }

    @Test
    void userSetters_ShouldUpdateFields() {

        User user = new User();

        user.setId(1L);
        user.setName("Jane");
        user.setEmail("jane@test.com");
        user.setAge(30);

        assertEquals(1L, user.getId());
        assertEquals("Jane", user.getName());
        assertEquals("jane@test.com", user.getEmail());
        assertEquals(30, user.getAge());
    }

    @Test
    void userEqualsAndHashCode_ShouldWork() {
        User user1 = new User("John", "john@test.com", 25);
        user1.setId(1L);

        User user2 = new User("John", "john@test.com", 25);
        user2.setId(1L);

        User user3 = new User("Jane", "jane@test.com", 30);
        user3.setId(2L);

        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getName(), user2.getName());
        assertEquals(user1.getEmail(), user2.getEmail());
        assertEquals(user1.getAge(), user2.getAge());

        assertNotEquals(user1.getId(), user3.getId());

    }

    @Test
    void userHashCode_WithDifferentCreatedAt_ShouldBeDifferent() {
        User user1 = new User("John", "john@test.com", 25);
        user1.setId(1L);

        User user2 = new User("John", "john@test.com", 25);
        user2.setId(1L);
        assertNotEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void userToString_ShouldContainFields() {
        User user = new User("John", "john@test.com", 25);
        user.setId(1L);
        String toString = user.toString();

        assertTrue(toString.contains("John"));
        assertTrue(toString.contains("john@test.com"));
        assertTrue(toString.contains("25"));
    }
}
