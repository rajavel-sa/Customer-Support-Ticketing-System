package com.Eval_Task.CSTS.service.impl;

import com.Eval_Task.CSTS.dto.TicketSummaryDTO;
import com.Eval_Task.CSTS.dto.UserResponseDTO;
import com.Eval_Task.CSTS.model.Ticket;
import com.Eval_Task.CSTS.model.User;
import com.Eval_Task.CSTS.repository.TicketRepository;
import com.Eval_Task.CSTS.repository.UserRepository;
import com.Eval_Task.CSTS.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private TicketRepository ticketRepository;

    private ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object data) {
        return ResponseEntity.status(status).body(
                new java.util.HashMap<String, Object>() {{
                    put("message", message);
                    put("status", status.value());
                    put("data", data);
                }}
        );
    }

    @Override
    public ResponseEntity<Object> getAllAgents() {
        try {
            List<User> agents = userRepository.findAllAgents();
            List<UserResponseDTO> agentDTOs = agents.stream()
                    .map(UserResponseDTO::new)
                    .collect(Collectors.toList());
            return generateResponse("Agents fetched successfully", HttpStatus.OK, agentDTOs);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse("Error fetching agents: " + e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @Override
    public ResponseEntity<Object> getTicketSummary() {
        try {
            long total = ticketRepository.count();
            long open = ticketRepository.countByStatus("OPEN");
            long assigned = ticketRepository.countByStatus("ASSIGNED");
            long resolved = ticketRepository.countByStatus("RESOLVED");

            TicketSummaryDTO summary = new TicketSummaryDTO(total, open, assigned, resolved);

            return generateResponse("Ticket summary fetched successfully", HttpStatus.OK, summary);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse("Error fetching ticket summary: " + e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @Override
    public ResponseEntity<Object> getUserProfile(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserResponseDTO dto = new UserResponseDTO(user);
            return generateResponse("User profile fetched successfully", HttpStatus.OK, dto);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse("Error fetching user profile: " + e.getMessage(), HttpStatus.NOT_FOUND, null);
        }
    }
}
