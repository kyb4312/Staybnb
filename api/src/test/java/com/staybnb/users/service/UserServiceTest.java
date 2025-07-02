package com.staybnb.users.service;

import com.staybnb.common.exception.custom.NoSuchUserException;
import com.staybnb.common.exception.custom.SignupException;
import com.staybnb.common.jwt.JwtUtils;
import com.staybnb.common.jwt.LogoutTokenService;
import com.staybnb.users.domain.User;
import com.staybnb.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private LogoutTokenService logoutTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("test@example.com", "Test User", "plainPassword");
        testUser.setId(1L);
    }

    @Nested
    @DisplayName("findById Method")
    class FindByIdTests {

        @Test
        @DisplayName("Should return user when found")
        void findById_whenUserExists_shouldReturnUser() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // Act
            User foundUser = userService.findById(1L);

            // Assert
            assertNotNull(foundUser);
            assertEquals(testUser.getId(), foundUser.getId());
            verify(userRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw NoSuchUserException when user not found")
        void findById_whenUserDoesNotExist_shouldThrowNoSuchUserException() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NoSuchUserException.class, () -> userService.findById(1L));
            verify(userRepository, times(1)).findById(1L);
        }
    }

    @Nested
    @DisplayName("login Method")
    class LoginTests {

        @BeforeEach
        void loginSetup() {
            String hashedPassword = BCrypt.hashpw("correctPassword", BCrypt.gensalt());
            testUser.setPassword(hashedPassword);
        }

        @Test
        @DisplayName("Should return JWT token for valid credentials")
        void login_withValidCredentials_shouldReturnToken() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
            when(jwtUtils.generateToken(testUser.getId().toString(), testUser.getName())).thenReturn("dummy.jwt.token");

            // Act
            String token = userService.login("test@example.com", "correctPassword");

            // Assert
            assertEquals("dummy.jwt.token", token);
            verify(userRepository, times(1)).findByEmail("test@example.com");
            verify(jwtUtils, times(1)).generateToken("1", "Test User");
        }

        @Test
        @DisplayName("Should throw NoSuchUserException for non-existent email")
        void login_withNonExistentEmail_shouldThrowNoSuchUserException() {
            // Arrange
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NoSuchUserException.class, () -> userService.login("wrong@example.com", "password"));
            verify(userRepository, times(1)).findByEmail("wrong@example.com");
        }

        @Test
        @DisplayName("Should throw NoSuchUserException for incorrect password")
        void login_withInvalidPassword_shouldThrowNoSuchUserException() {
            // Arrange
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // Act & Assert
            assertThrows(NoSuchUserException.class, () -> userService.login("test@example.com", "wrongPassword"));
            verify(userRepository, times(1)).findByEmail("test@example.com");
        }

        @Test
        @DisplayName("Should throw NoSuchUserException for a deleted user")
        void login_withDeletedUser_shouldThrowNoSuchUserException() {
            // Arrange
            testUser.setDeleted(true);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

            // Act & Assert
            assertThrows(NoSuchUserException.class, () -> userService.login("test@example.com", "correctPassword"));
            verify(userRepository, times(1)).findByEmail("test@example.com");
        }
    }

    @Nested
    @DisplayName("logout Method")
    class LogoutTests {

        @Test
        @DisplayName("logout should call LogoutTokenService")
        void logout_shouldCallLogoutTokenService() {
            // Arrange
            String token = "dummy.jwt.token";

            // Act
            userService.logout(token);

            // Assert
            verify(logoutTokenService, times(1)).logout(token);
        }
    }

    @Nested
    @DisplayName("signup Method")
    class SignupTests {

        @Test
        @DisplayName("Should create and return a new user with a hashed password")
        void signup_withNewEmail_shouldCreateAndReturnUser() {
            // Arrange
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.empty());
            // Use thenAnswer to return the same user object that was passed to save()
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            User signedUpUser = userService.signup(testUser);

            // Assert
            assertNotNull(signedUpUser);
            assertEquals("test@example.com", signedUpUser.getEmail());
            // Verify password was hashed and is not the plain text version
            assertNotEquals("plainPassword", signedUpUser.getPassword());
            assertTrue(BCrypt.checkpw("plainPassword", signedUpUser.getPassword()));
            verify(userRepository, times(1)).findByEmail(testUser.getEmail());
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw SignupException if email already exists")
        void signup_withExistingEmail_shouldThrowSignupException() {
            // Arrange
            when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(new User("test2@example.com", "existingUser", "password")));

            // Act & Assert
            assertThrows(SignupException.class, () -> userService.signup(testUser));

            verify(userRepository, times(1)).findByEmail(testUser.getEmail());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteAccount Method")
    class DeleteAccountTests {

        @Test
        @DisplayName("Should mark user as deleted and set deletion timestamp")
        void deleteAccount_whenUserExists_shouldMarkUserAsDeleted() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            assertFalse(testUser.isDeleted()); // Pre-condition
            assertNull(testUser.getDeletedAt()); // Pre-condition

            // Act
            userService.deleteAccount(1L);

            // Assert
            verify(userRepository, times(1)).findById(1L);
            assertTrue(testUser.isDeleted());
            assertNotNull(testUser.getDeletedAt());
            // Verify the timestamp is recent
            assertTrue(testUser.getDeletedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
            assertTrue(testUser.getDeletedAt().isAfter(LocalDateTime.now().minusSeconds(1)));
        }

        @Test
        @DisplayName("Should throw NoSuchUserException when user to delete is not found")
        void deleteAccount_whenUserDoesNotExist_shouldThrowException() {
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NoSuchUserException.class, () -> userService.deleteAccount(1L));
            verify(userRepository, times(1)).findById(1L);
        }
    }
}