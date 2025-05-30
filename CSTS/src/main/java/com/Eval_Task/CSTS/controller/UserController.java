package com.Eval_Task.CSTS.controller;

import com.Eval_Task.CSTS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired private UserService userService;

    @GetMapping("/agents")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public ResponseEntity<Object> getAllAgents() {
        return userService.getAllAgents();
    }

    @GetMapping("/tickets/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<Object> getTicketSummary() {
        return userService.getTicketSummary();
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<Object> getUserProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }
}