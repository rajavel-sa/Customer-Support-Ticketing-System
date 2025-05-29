package com.Eval_Task.CSTS.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.Eval_Task.CSTS.dto.*;
import com.Eval_Task.CSTS.model.*;
import com.Eval_Task.CSTS.repository.*;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

    @Mock private TicketRepository ticketRepo;
    @Mock private UserRepository userRepo;
    @Mock private CommentRepository commentRepo;

    @InjectMocks private TicketServiceImpl ticketService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(String username) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(username);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void createTicket_Success() {
        mockAuthenticatedUser("customer1");

        User user = new User();
        user.setId(1L);
        user.setUsername("customer1");

        TicketRequest request = new TicketRequest();
        request.setTitle("Test Title");
        request.setDescription("Test Description");

        when(userRepo.findByUsername("customer1")).thenReturn(user);
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        ResponseEntity<Object> response = ticketService.createTicket(request);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals("Ticket created successfully", body.get("message"));
        assertNotNull(body.get("data"));
    }

    @Test
    void getMyTickets_Customer() {
        mockAuthenticatedUser("customer1");

        User mockUser = new User();
        mockUser.setUsername("customer1");

        Role customerRole = new Role();
        customerRole.setRole("ROLE_CUSTOMER");
        mockUser.setRole(customerRole);

        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setTitle("Test Ticket");
        ticket1.setCreatedBy(mockUser);

        when(userRepo.findByUsername("customer1")).thenReturn(mockUser);
        when(ticketRepo.findByCreatedBy(mockUser)).thenReturn(Collections.singletonList(ticket1));

        ResponseEntity<Object> response = ticketService.getMyTickets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> responseBody = (Map<?, ?>) response.getBody();
        List<?> tickets = (List<?>) responseBody.get("data");
        assertEquals(1, tickets.size());
    }

    @Test
    void assignTicket_Success() {
        mockAuthenticatedUser("agent1");

        Role agentRole = new Role();
        agentRole.setRole("ROLE_AGENT");

        User agent = new User();
        agent.setId(101L);
        agent.setUsername("agent1");
        agent.setRole(agentRole);
        agent.setPassword("dummy");

        Ticket openTicket = new Ticket();
        openTicket.setId(1L);
        openTicket.setStatus("OPEN");
        openTicket.setCreatedBy(agent);  // <--- Add this line to fix NPE

        when(userRepo.findByUsername("agent1")).thenReturn(agent);
        when(ticketRepo.findById(1L)).thenReturn(Optional.of(openTicket));
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<Object> response = ticketService.assignTicket(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(ticketRepo).save(argThat(ticket ->
                "ASSIGNED".equals(ticket.getStatus()) &&
                        ticket.getAssignedTo() != null &&
                        ticket.getAssignedTo().getId().equals(agent.getId())
        ));
    }

    @Test
    void addComment_Success() {
        mockAuthenticatedUser("user1");

        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setCreatedBy(user);
        ticket.setUpdatedAt(LocalDateTime.now());

        when(userRepo.findByUsername("user1")).thenReturn(user);
        when(ticketRepo.findById(1L)).thenReturn(Optional.of(ticket));
        when(commentRepo.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId(1L);
            return comment;
        });
        when(commentRepo.findById(1L)).thenAnswer(invocation -> {
            Comment comment = new Comment();
            comment.setId(1L);
            comment.setContent("Test comment");
            comment.setUser(user);
            comment.setTicket(ticket);
            comment.setCreatedAt(LocalDateTime.now());
            return Optional.of(comment);
        });
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CommentRequest request = new CommentRequest("Test comment");

        ResponseEntity<Object> response = ticketService.addComment(1L, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Comment added successfully", responseBody.get("message"));
        assertNotNull(responseBody.get("data"));
        CommentResponseDTO responseDTO = (CommentResponseDTO) responseBody.get("data");
        assertEquals("Test comment", responseDTO.getContent());
        assertEquals("user1", responseDTO.getUsername());

        verify(commentRepo).save(argThat(comment ->
                comment.getContent().equals("Test comment") &&
                        comment.getUser().equals(user) &&
                        comment.getTicket().equals(ticket)
        ));
        verify(ticketRepo).save(argThat(t -> t.getUpdatedAt() != null));
    }

    @Test
    void resolveTicket_AgentSuccess() {
        mockAuthenticatedUser("agent1");

        User agent = new User();
        agent.setId(1L);
        agent.setUsername("agent1");
        Role agentRole = new Role();
        agentRole.setRole("ROLE_AGENT");
        agent.setRole(agentRole);

        User createdBy = new User();
        createdBy.setId(2L);
        createdBy.setUsername("customer1");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus("ASSIGNED");
        ticket.setAssignedTo(agent);
        ticket.setCreatedBy(createdBy);

        when(userRepo.findByUsername("agent1")).thenReturn(agent);
        when(ticketRepo.findById(1L)).thenReturn(Optional.of(ticket));
        when(ticketRepo.save(any(Ticket.class))).thenAnswer(invocation -> {
            Ticket t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });

        ResponseEntity<Object> response = ticketService.resolveTicket(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Ticket resolved successfully", responseBody.get("message"));
        assertNotNull(responseBody.get("data"));
        TicketResponseDTO responseDTO = (TicketResponseDTO) responseBody.get("data");
        assertEquals("RESOLVED", responseDTO.getStatus());
        assertNotNull(responseDTO.getResolvedAt());
        assertNotNull(responseDTO.getUpdatedAt());

        verify(ticketRepo).save(argThat(t ->
                "RESOLVED".equals(t.getStatus()) &&
                        t.getResolvedAt() != null &&
                        t.getUpdatedAt() != null &&
                        t.getAssignedTo().equals(agent)
        ));
    }

    @Test
    void createTicket_UnauthenticatedUser() {
        SecurityContextHolder.clearContext();

        TicketRequest request = new TicketRequest();
        request.setTitle("Test Title");
        request.setDescription("Test Description");

        ResponseEntity<Object> response = ticketService.createTicket(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Error creating ticket: Cannot invoke \"org.springframework.security.core.Authentication.getName()\" because \"authentication\" is null", responseBody.get("message"));
        verifyNoInteractions(userRepo, ticketRepo);
    }

    @Test
    void getMyTickets_Agent() {
        mockAuthenticatedUser("agent1");

        User mockUser = new User();
        mockUser.setId(101L);
        mockUser.setUsername("agent1");
        Role agentRole = new Role();
        agentRole.setRole("ROLE_AGENT");
        mockUser.setRole(agentRole);

        User createdBy = new User();
        createdBy.setId(102L);
        createdBy.setUsername("customer1");

        Ticket ticket1 = new Ticket();
        ticket1.setId(1L);
        ticket1.setTitle("Agent Ticket");
        ticket1.setAssignedTo(mockUser);
        ticket1.setCreatedBy(createdBy);

        when(userRepo.findByUsername("agent1")).thenReturn(mockUser);
        when(ticketRepo.findByAssignedTo(mockUser)).thenReturn(Collections.singletonList(ticket1));

        ResponseEntity<Object> response = ticketService.getMyTickets();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        @SuppressWarnings("unchecked")
        List<TicketResponseDTO> tickets = (List<TicketResponseDTO>) responseBody.get("data");
        assertEquals(1, tickets.size());
        assertEquals("Agent Ticket", tickets.get(0).getTitle());
    }

    @Test
    void assignTicket_TicketNotFound() {
        mockAuthenticatedUser("agent1");

        User agent = new User();
        agent.setId(101L);
        agent.setUsername("agent1");
        Role agentRole = new Role();
        agentRole.setRole("ROLE_AGENT");
        agent.setRole(agentRole);

        when(userRepo.findByUsername("agent1")).thenReturn(agent);
        when(ticketRepo.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = ticketService.assignTicket(1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Error assigning ticket: Ticket not found", responseBody.get("message"));
        verifyNoMoreInteractions(ticketRepo);
    }

    @Test
    void addComment_TicketNotFound() {
        mockAuthenticatedUser("user1");

        User user = new User();
        user.setId(1L);
        user.setUsername("user1");

        when(userRepo.findByUsername("user1")).thenReturn(user);
        when(ticketRepo.findById(1L)).thenReturn(Optional.empty());

        CommentRequest request = new CommentRequest("Test comment");

        ResponseEntity<Object> response = ticketService.addComment(1L, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Error adding comment: Ticket not found", responseBody.get("message"));
        verifyNoInteractions(commentRepo);
    }

    @Test
    void resolveTicket_NotAssigned() {
        mockAuthenticatedUser("agent1");

        User agent = new User();
        agent.setId(1L);
        agent.setUsername("agent1");
        Role agentRole = new Role();
        agentRole.setRole("ROLE_AGENT");
        agent.setRole(agentRole);

        User createdBy = new User();
        createdBy.setId(2L);
        createdBy.setUsername("customer1");

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        ticket.setStatus("OPEN");
        ticket.setCreatedBy(createdBy);

        when(userRepo.findByUsername("agent1")).thenReturn(agent);
        when(ticketRepo.findById(1L)).thenReturn(Optional.of(ticket));

        ResponseEntity<Object> response = ticketService.resolveTicket(1L);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals("Only assigned agent or admin can resolve tickets", responseBody.get("message"));
        verifyNoMoreInteractions(ticketRepo);
    }

}
