package com.Eval_Task.CSTS.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.Collectors;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.Eval_Task.CSTS.config.JwtGeneratorValidator;
import com.Eval_Task.CSTS.dto.UserDto;
import com.Eval_Task.CSTS.model.Role;
import com.Eval_Task.CSTS.model.User;
import com.Eval_Task.CSTS.repository.RoleRepository;
import com.Eval_Task.CSTS.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private RoleRepository roleRepo; // Added missing mock
    @Mock private AuthenticationManager authManager;
    @Mock private JwtGeneratorValidator jwtGenVal;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_Success() {
        // 1. Setup test data
        UserDto userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setPassword("password");

        // 2. Create a single Role
        Role userRole = new Role();
        userRole.setRole("ROLE_USER");

        // 3. Setup mock user with single role
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPass");
        mockUser.setRole(userRole);

        // 4. Mock dependencies
        when(userRepo.findByUsername("testuser")).thenReturn(mockUser);

        // 5. Create authentication with single authority
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(userRole.getRole())
        );

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "testuser", "password", authorities);

        when(authManager.authenticate(any())).thenReturn(auth);
        when(jwtGenVal.generateToken(auth)).thenReturn("fakeJwtToken");

        // 6. Set authentication in SecurityContext
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 7. Call method
        ResponseEntity<Object> response = authService.login(userDto);

        // 8. Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Extract response body
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Login successful", responseBody.get("message"));
        assertEquals("fakeJwtToken", responseBody.get("data"));

        verify(authManager, times(1)).authenticate(any());
    }

    @Test
    void login_WrongPassword() {
        // Setup test data
        UserDto userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setPassword("wrongpass");

        // Setup mock user
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPass");

        // Mock dependencies
        when(userRepo.findByUsername("testuser")).thenReturn(mockUser);
        when(authManager.authenticate(any())).thenThrow(new RuntimeException("Authentication failed"));

        // Call method
        ResponseEntity<Object> response = authService.login(userDto);

        // Verify
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Password is wrong", responseBody.get("message"));
    }

    @Test
    void refreshToken_ValidToken() throws Exception {
        // 1. Mock JWT claims
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn("testuser");
        when(jwtGenVal.extractAllClaims("validToken")).thenReturn(mockClaims);
        when(jwtGenVal.isTokenExpired("validToken")).thenReturn(false);

        // 2. Mock user loading
        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("pass");
        Role userRole = new Role();
        userRole.setRole("ROLE_USER");
        mockUser.setRole(userRole); // Set single Role object
        when(userRepo.findByUsername("testuser")).thenReturn(mockUser);

        // 3. Mock new token generation
        Authentication auth = new UsernamePasswordAuthenticationToken(mockUser, null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        when(jwtGenVal.generateToken(any())).thenReturn("newAccessToken");
        when(jwtGenVal.generateRefreshToken(any())).thenReturn("newRefreshToken");

        // 4. Call method
        ResponseEntity<Object> response = authService.refreshToken("validToken");

        // 5. Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertEquals("newAccessToken", responseBody.get("accessToken"));
        assertEquals("newRefreshToken", responseBody.get("refreshToken"));
    }

    @Test
    void refreshToken_InvalidToken() throws Exception {
        when(jwtGenVal.extractAllClaims("invalidToken")).thenThrow(new RuntimeException("Invalid token"));

        ResponseEntity<Object> response = authService.refreshToken("invalidToken");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Invalid refresh token", responseBody.get("message"));
        verifyNoInteractions(userRepo);
    }

    @Test
    void refreshToken_MalformedToken() {
        when(jwtGenVal.extractAllClaims("malformedToken")).thenThrow(new IllegalArgumentException("Malformed token"));

        ResponseEntity<Object> response = authService.refreshToken("malformedToken");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Invalid refresh token", responseBody.get("message"));
        verifyNoInteractions(userRepo);
    }

    @Test
    void login_UsernameCaseSensitivity() {
        UserDto userDto = new UserDto();
        userDto.setUsername("TestUser");
        userDto.setPassword("password");

        User mockUser = new User();
        mockUser.setUsername("testuser");
        mockUser.setPassword("encodedPass");
        Role userRole = new Role();
        userRole.setRole("ROLE_USER");
        mockUser.setRole(userRole);

        when(userRepo.findByUsername("TestUser")).thenReturn(mockUser);
        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        when(authManager.authenticate(any())).thenReturn(auth);
        when(jwtGenVal.generateToken(auth)).thenReturn("caseSensitiveToken");

        ResponseEntity<Object> response = authService.login(userDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Login successful", responseBody.get("message"));
        assertEquals("caseSensitiveToken", responseBody.get("data"));
        verify(authManager).authenticate(any());
    }

    @Test
    void refreshToken_EmptyToken() {
        ResponseEntity<Object> response = authService.refreshToken("");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Invalid refresh token", responseBody.get("message"));
    }

    @Test
    void login_DisabledUser() {
        UserDto userDto = new UserDto();
        userDto.setUsername("disableduser");
        userDto.setPassword("password");

        User mockUser = new User();
        mockUser.setUsername("disableduser");
        mockUser.setPassword("encodedPass");
        Role userRole = new Role();
        userRole.setRole("ROLE_USER");
        mockUser.setRole(userRole);

        when(userRepo.findByUsername("disableduser")).thenReturn(mockUser);
        when(authManager.authenticate(any())).thenThrow(new RuntimeException("Password is wrong"));

        ResponseEntity<Object> response = authService.login(userDto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Password is wrong", responseBody.get("message"));
        verify(authManager).authenticate(any());
    }

    @Test
    void refreshToken_UserNotFound() throws Exception {
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn("nonexistentuser");
        when(jwtGenVal.extractAllClaims("validToken")).thenReturn(mockClaims);

        ResponseEntity<Object> response = authService.refreshToken("validToken");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Invalid refresh token", responseBody.get("message"));
    }

    @Test
    void refreshToken_ExpiredToken() throws Exception {
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getSubject()).thenReturn("testuser");
        when(jwtGenVal.extractAllClaims("expiredToken")).thenReturn(mockClaims);

        ResponseEntity<Object> response = authService.refreshToken("expiredToken");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Invalid refresh token", responseBody.get("message"));
    }

    @Test
    void login_EmptyCredentials() {
        UserDto userDto = new UserDto();
        userDto.setUsername("");
        userDto.setPassword("");

        when(userRepo.findByUsername("")).thenReturn(null);

        ResponseEntity<Object> response = authService.login(userDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Username does not exist", responseBody.get("message"));
        verifyNoInteractions(authManager);
    }

    @Test
    void login_NullCredentials() {
        UserDto userDto = new UserDto();
        userDto.setUsername(null);
        userDto.setPassword(null);

        when(userRepo.findByUsername(null)).thenReturn(null);

        ResponseEntity<Object> response = authService.login(userDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Username does not exist", responseBody.get("message"));
        verifyNoInteractions(authManager);
    }

    @Test
    void login_UserNotFound() {
        UserDto userDto = new UserDto();
        userDto.setUsername("nonexistentuser");
        userDto.setPassword("password");

        when(userRepo.findByUsername("nonexistentuser")).thenReturn(null);

        ResponseEntity<Object> response = authService.login(userDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Username does not exist", responseBody.get("message"));
        verifyNoInteractions(authManager);
    }
}