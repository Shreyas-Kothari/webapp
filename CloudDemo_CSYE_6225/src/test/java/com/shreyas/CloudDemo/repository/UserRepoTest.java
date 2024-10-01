package com.shreyas.CloudDemo.repository;

import com.shreyas.CloudDemo.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class UserRepoTest {

    @Mock
    private UserRepo userRepo;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("john.doe@example.com");
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        when(userRepo.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        Optional<User> foundUser = userRepo.findByEmail("john.doe@example.com");

        assertTrue(foundUser.isPresent());
        assertEquals(user.getEmail(), foundUser.get().getEmail());
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenUserDoesNotExist() {
        when(userRepo.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        Optional<User> foundUser = userRepo.findByEmail("unknown@example.com");

        assertFalse(foundUser.isPresent());
    }
}

