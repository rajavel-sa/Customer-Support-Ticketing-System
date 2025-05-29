package com.Eval_Task.CSTS.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

public interface UserService {
    ResponseEntity<Object> getAllAgents();
    ResponseEntity<Object> getTicketSummary();
    ResponseEntity<Object> getUserProfile(Long id);
}
