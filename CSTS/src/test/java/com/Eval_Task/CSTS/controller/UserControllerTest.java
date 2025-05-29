package com.Eval_Task.CSTS.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.Eval_Task.CSTS.config.SecurityConfig;
import com.Eval_Task.CSTS.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock private UserService userService;

    @InjectMocks private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();
        objectMapper = new ObjectMapper();
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllAgents_Success() throws Exception {
        List<Map<String, Object>> agents = new ArrayList<>();
        Map<String, Object> agent = new HashMap<>();
        agent.put("id", 1L);
        agent.put("username", "agent1");
        agents.add(agent);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Agents retrieved successfully");
        responseBody.put("data", agents);

        when(userService.getAllAgents())
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Agents retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].username").value("agent1"));

        verify(userService).getAllAgents();
    }

    @Test
    void getAllAgents_NoAgents() throws Exception {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "No agents found");
        responseBody.put("data", new ArrayList<>());

        when(userService.getAllAgents())
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(get("/api/agents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No agents found"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(userService).getAllAgents();
    }

    @Test
    void getTicketSummary_Success() throws Exception {
        Map<String, Integer> summary = new HashMap<>();
        summary.put("open", 5);
        summary.put("assigned", 3);
        summary.put("resolved", 2);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Ticket summary retrieved successfully");
        responseBody.put("data", summary);

        when(userService.getTicketSummary())
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(get("/api/tickets/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ticket summary retrieved successfully"))
                .andExpect(jsonPath("$.data.open").value(5))
                .andExpect(jsonPath("$.data.assigned").value(3))
                .andExpect(jsonPath("$.data.resolved").value(2));

        verify(userService).getTicketSummary();
    }

    @Test
    void getUserProfile_Success() throws Exception {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", 1L);
        userData.put("username", "user1");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "User profile retrieved successfully");
        responseBody.put("data", userData);

        when(userService.getUserProfile(1L))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User profile retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.username").value("user1"));

        verify(userService).getUserProfile(1L);
    }

    @Test
    void getUserProfile_NotFound() throws Exception {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "User not found");

        when(userService.getUserProfile(1L))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(userService).getUserProfile(1L);
    }
}