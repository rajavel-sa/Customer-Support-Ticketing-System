package com.Eval_Task.CSTS.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.Eval_Task.CSTS.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.Eval_Task.CSTS.dto.TicketSummaryDTO;
import com.Eval_Task.CSTS.dto.UserResponseDTO;
import com.Eval_Task.CSTS.model.User;
import com.Eval_Task.CSTS.repository.TicketRepository;
import com.Eval_Task.CSTS.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAllAgents_Success() {
        User agent1 = new User();
        agent1.setId(1L);
        agent1.setUsername("agent1");

        User agent2 = new User();
        agent2.setId(2L);
        agent2.setUsername("agent2");

        List<User> mockAgents = Arrays.asList(agent1, agent2);

        when(userRepository.findAllAgents()).thenReturn(mockAgents);

        ResponseEntity<Object> response = userService.getAllAgents();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        List<UserResponseDTO> agents = (List<UserResponseDTO>) ((Map) response.getBody()).get("data");
        assertEquals(2, agents.size());
        assertEquals("agent1", agents.get(0).getUsername());

        verify(userRepository, times(1)).findAllAgents();
    }

    @Test
    void getTicketSummary_Success() {
        when(ticketRepository.count()).thenReturn(10L);
        when(ticketRepository.countByStatus("OPEN")).thenReturn(3L);
        when(ticketRepository.countByStatus("ASSIGNED")).thenReturn(5L);
        when(ticketRepository.countByStatus("RESOLVED")).thenReturn(2L);

        ResponseEntity<Object> response = userService.getTicketSummary();

        assertEquals(HttpStatus.OK, response.getStatusCode());

        TicketSummaryDTO summary = (TicketSummaryDTO) ((Map) response.getBody()).get("data");
        assertEquals(10, summary.getTotalTickets());
        assertEquals(3, summary.getOpenTickets());
        assertEquals(5, summary.getAssignedTickets());
        assertEquals(2, summary.getResolvedTickets());

        verify(ticketRepository, times(1)).count();
        verify(ticketRepository, times(1)).countByStatus("OPEN");
    }

    @Test
    void getUserProfile_Success() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        ResponseEntity<Object> response = userService.getUserProfile(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        UserResponseDTO dto = (UserResponseDTO) ((Map) response.getBody()).get("data");
        assertEquals("testuser", dto.getUsername());
    }

    @Test
    void getUserProfile_NotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = userService.getUserProfile(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("User not found"));
    }

    @Test
    void getAllAgents_EmptyList() {
        when(userRepository.findAllAgents()).thenReturn(Collections.emptyList());

        ResponseEntity<Object> response = userService.getAllAgents();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        List<UserResponseDTO> agents = (List<UserResponseDTO>) ((Map<String, Object>) response.getBody()).get("data");
        assertTrue(agents.isEmpty());
        verify(userRepository, times(1)).findAllAgents();
    }

    @Test
    void getTicketSummary_NoTickets() {
        when(ticketRepository.count()).thenReturn(0L);
        when(ticketRepository.countByStatus("OPEN")).thenReturn(0L);
        when(ticketRepository.countByStatus("ASSIGNED")).thenReturn(0L);
        when(ticketRepository.countByStatus("RESOLVED")).thenReturn(0L);

        ResponseEntity<Object> response = userService.getTicketSummary();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        TicketSummaryDTO summary = (TicketSummaryDTO) ((Map<String, Object>) response.getBody()).get("data");
        assertEquals(0, summary.getTotalTickets());
        assertEquals(0, summary.getOpenTickets());
        assertEquals(0, summary.getAssignedTickets());
        assertEquals(0, summary.getResolvedTickets());
        verify(ticketRepository, times(1)).count();
        verify(ticketRepository, times(1)).countByStatus("OPEN");
        verify(ticketRepository, times(1)).countByStatus("ASSIGNED");
        verify(ticketRepository, times(1)).countByStatus("RESOLVED");
    }

    @Test
    void getTicketSummary_PartialStatusCounts() {
        when(ticketRepository.count()).thenReturn(5L);
        when(ticketRepository.countByStatus("OPEN")).thenReturn(2L);
        when(ticketRepository.countByStatus("ASSIGNED")).thenReturn(0L);
        when(ticketRepository.countByStatus("RESOLVED")).thenReturn(3L);

        ResponseEntity<Object> response = userService.getTicketSummary();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        TicketSummaryDTO summary = (TicketSummaryDTO) ((Map<String, Object>) response.getBody()).get("data");
        assertEquals(5, summary.getTotalTickets());
        assertEquals(2, summary.getOpenTickets());
        assertEquals(0, summary.getAssignedTickets());
        assertEquals(3, summary.getResolvedTickets());
        verify(ticketRepository, times(1)).count();
        verify(ticketRepository, times(1)).countByStatus("OPEN");
        verify(ticketRepository, times(1)).countByStatus("ASSIGNED");
        verify(ticketRepository, times(1)).countByStatus("RESOLVED");
    }

    @Test
    void getUserProfile_NullId() {
        when(userRepository.findById(null)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = userService.getUserProfile(null);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Error fetching user profile: User not found", responseBody.get("message"));
    }

}
