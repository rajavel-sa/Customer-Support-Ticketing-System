package com.Eval_Task.CSTS.service;

import com.Eval_Task.CSTS.dto.CommentRequest;
import com.Eval_Task.CSTS.dto.TicketRequest;
import org.springframework.http.ResponseEntity;

public interface TicketService {
    ResponseEntity<Object> createTicket(TicketRequest request);
    ResponseEntity<Object> getMyTickets();
    ResponseEntity<Object> getOpenTickets();
    ResponseEntity<Object> assignTicket(Long id);
    ResponseEntity<Object> resolveTicket(Long id);
    ResponseEntity<Object> addComment(Long id, CommentRequest comment);
}