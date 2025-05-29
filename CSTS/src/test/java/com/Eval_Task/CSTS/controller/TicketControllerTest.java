package com.Eval_Task.CSTS.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.Eval_Task.CSTS.dto.CommentRequest;
import com.Eval_Task.CSTS.dto.TicketRequest;
import com.Eval_Task.CSTS.service.TicketService;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class TicketControllerTest {

    @Mock private TicketService ticketService;

    @InjectMocks private TicketController ticketController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ticketController).build();
        objectMapper = new ObjectMapper();
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTicket_Success() throws Exception {
        TicketRequest request = new TicketRequest();
        request.setTitle("Test Ticket");
        request.setDescription("Test Description");

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("id", 1L);
        ticketData.put("title", "Test Ticket");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Ticket created successfully");
        responseBody.put("data", ticketData);

        when(ticketService.createTicket(any(TicketRequest.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(post("/api/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ticket created successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("Test Ticket"));

        verify(ticketService).createTicket(any(TicketRequest.class));
    }

    @Test
    void getMyTickets_Success() throws Exception {
        List<Map<String, Object>> tickets = new ArrayList<>();
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("id", 1L);
        ticket.put("title", "Ticket 1");
        tickets.add(ticket);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Tickets retrieved successfully");
        responseBody.put("data", tickets);

        when(ticketService.getMyTickets())
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(get("/api/tickets/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tickets retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("Ticket 1"));

        verify(ticketService).getMyTickets();
    }

    @Test
    void getOpenTickets_Success() throws Exception {
        List<Map<String, Object>> tickets = new ArrayList<>();
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("id", 1L);
        ticket.put("title", "Open Ticket");
        tickets.add(ticket);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Open tickets retrieved successfully");
        responseBody.put("data", tickets);

        when(ticketService.getOpenTickets())
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(get("/api/tickets/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Open tickets retrieved successfully"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].title").value("Open Ticket"));

        verify(ticketService).getOpenTickets();
    }

    @Test
    void assignTicket_Success() throws Exception {
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("id", 1L);
        ticketData.put("status", "ASSIGNED");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Ticket assigned successfully");
        responseBody.put("data", ticketData);

        when(ticketService.assignTicket(1L))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(put("/api/tickets/1/assign"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ticket assigned successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("ASSIGNED"));

        verify(ticketService).assignTicket(1L);
    }

    @Test
    void assignTicket_NotFound() throws Exception {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Ticket not found");

        when(ticketService.assignTicket(1L))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND));

        mockMvc.perform(put("/api/tickets/1/assign"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found"));

        verify(ticketService).assignTicket(1L);
    }

    @Test
    void resolveTicket_Success() throws Exception {
        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("id", 1L);
        ticketData.put("status", "RESOLVED");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Ticket resolved successfully");
        responseBody.put("data", ticketData);

        when(ticketService.resolveTicket(1L))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(put("/api/tickets/1/resolve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ticket resolved successfully"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));

        verify(ticketService).resolveTicket(1L);
    }

    @Test
    void addComment_Success() throws Exception {
        Map<String, Object> commentData = new HashMap<>();
        commentData.put("ticketId", 1L);
        commentData.put("content", "Test comment");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Comment added successfully");
        responseBody.put("data", commentData);

        when(ticketService.addComment(eq(1L), any(CommentRequest.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        CommentRequest comment = new CommentRequest();
        comment.setContent("Test comment");

        mockMvc.perform(post("/api/tickets/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment added successfully"))
                .andExpect(jsonPath("$.data.ticketId").value(1L))
                .andExpect(jsonPath("$.data.content").value("Test comment"));

        verify(ticketService).addComment(eq(1L), any(CommentRequest.class));
    }

    @Test
    void getMyTickets_EmptyList() throws Exception {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "No tickets found");
        responseBody.put("data", new ArrayList<>());

        when(ticketService.getMyTickets())
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(get("/api/tickets/mine")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No tickets found"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(ticketService).getMyTickets();
    }

    @Test
    void getOpenTickets_EmptyList() throws Exception {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "No open tickets found");
        responseBody.put("data", new ArrayList<>());

        when(ticketService.getOpenTickets())
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        mockMvc.perform(get("/api/tickets/open")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No open tickets found"))
                .andExpect(jsonPath("$.data").isEmpty());

        verify(ticketService).getOpenTickets();
    }

    @Test
    void assignTicket_AlreadyAssigned() throws Exception {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Ticket already assigned");
        when(ticketService.assignTicket(6L))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST));

        mockMvc.perform(put("/api/tickets/6/assign")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ticket already assigned"));

        verify(ticketService).assignTicket(6L);
    }

    @Test
    void resolveTicket_AlreadyResolved() throws Exception {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Ticket already resolved");

        when(ticketService.resolveTicket(7L))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST));

        mockMvc.perform(put("/api/tickets/7/resolve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ticket already resolved"));

        verify(ticketService).resolveTicket(7L);
    }

    @Test
    void addComment_TicketNotFound() throws Exception {
        CommentRequest comment = new CommentRequest();
        comment.setContent("Invalid ticket comment");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Ticket not found");

        when(ticketService.addComment(eq(999L), any(CommentRequest.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/api/tickets/999/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found"));

        verify(ticketService).addComment(eq(999L), any(CommentRequest.class));
    }

    @Test
    void addComment_EmptyContent() throws Exception {
        CommentRequest comment = new CommentRequest();
        comment.setContent("");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Comment content cannot be empty");

        when(ticketService.addComment(eq(10L), any(CommentRequest.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.BAD_REQUEST));

        mockMvc.perform(post("/api/tickets/10/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Comment content cannot be empty"));

        verify(ticketService).addComment(eq(10L), any(CommentRequest.class));
    }

    @Test
    void resolveTicket_ServiceError() throws Exception {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Internal server error");

        when(ticketService.resolveTicket(11L))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.INTERNAL_SERVER_ERROR));

        mockMvc.perform(put("/api/tickets/11/resolve")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Internal server error"));

        verify(ticketService).resolveTicket(11L);
    }
}