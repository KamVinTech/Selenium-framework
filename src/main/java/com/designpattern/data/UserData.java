package com.designpattern.data;

import lombok.Builder;
import lombok.Getter;

/**
 * Test Data class using Builder Pattern for User data
 */
@Getter
@Builder
public class UserData {
    private String username;
    private String password;
    private String email;
    private String firstName;
    private String lastName;

    /**
     * Creates a default test user
     * @return UserData instance with default values
     */
    public static UserData createDefaultUser() {
        return UserData.builder()
                .username("testuser")
                .password("password123")
                .email("testuser@example.com")
                .firstName("Test")
                .lastName("User")
                .build();
    }
}