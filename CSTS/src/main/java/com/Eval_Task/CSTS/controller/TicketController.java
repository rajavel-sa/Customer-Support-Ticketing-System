package com.Eval_Task.CSTS.controller;

import com.Eval_Task.CSTS.dto.CommentRequest;
import com.Eval_Task.CSTS.dto.TicketRequest;
import com.Eval_Task.CSTS.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    @Autowired private TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Object> createTicket(@RequestBody TicketRequest request) {
        return ticketService.createTicket(request);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('AGENT', 'CUSTOMER', 'ADMIN')")
    public ResponseEntity<Object> getMyTickets() {
        return ticketService.getMyTickets();
    }

    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<?> getOpenTickets() {
        return ticketService.getOpenTickets();
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<Object> assignTicket(@PathVariable Long id) {
        return ticketService.assignTicket(id);
    }

    @PutMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    public ResponseEntity<Object> resolveTicket(@PathVariable Long id) {
        return ticketService.resolveTicket(id);
    }

    @PostMapping("/{id}/comment")
    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN','CUSTOMER')")
    public ResponseEntity<Object> addComment(@PathVariable Long id, @RequestBody CommentRequest comment) {
        return ticketService.addComment(id, comment);
    }
}