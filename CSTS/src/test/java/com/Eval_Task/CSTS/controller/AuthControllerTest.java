package com.Eval_Task.CSTS.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.Eval_Task.CSTS.dto.UserDto;
import com.Eval_Task.CSTS.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock private AuthService authService;

    @InjectMocks private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_Success() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setPassword("password");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Login successful");
        responseBody.put("data", "fakeJwtToken");

        when(authService.login(any(UserDto.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data").value("fakeJwtToken"));

        verify(authService).login(any(UserDto.class));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        UserDto userDto = new UserDto();
        userDto.setUsername("testuser");
        userDto.setPassword("wrongpass");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Password is wrong");

        when(authService.login(any(UserDto.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Password is wrong"));

        verify(authService).login(any(UserDto.class));
    }

    @Test
    void refreshToken_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "validToken");

        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("accessToken", "newAccessToken");
        tokenData.put("refreshToken", "newRefreshToken");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Token refreshed successfully");
        responseBody.put("data", tokenData);

        when(authService.refreshToken("validToken"))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Token refreshed successfully"))
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("newRefreshToken"));

        verify(authService).refreshToken("validToken");
    }

    @Test
    void refreshToken_InvalidToken() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("refreshToken", "invalidToken");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Invalid refresh token");

        when(authService.refreshToken("invalidToken"))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/auth/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));

        verify(authService).refreshToken("invalidToken");
    }
}