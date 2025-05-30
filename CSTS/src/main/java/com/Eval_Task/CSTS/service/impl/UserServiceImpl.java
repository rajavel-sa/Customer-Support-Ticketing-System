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
import java.util.*;
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

            List<Map<String, Object>> agentData = new ArrayList<>();

            for (User agent : agents) {
                Map<String, Object> agentInfo = new HashMap<>();
                agentInfo.put("id", agent.getId());
                agentInfo.put("username", agent.getUsername());

                Long totalAssigned = ticketRepository.countByAssignedTo(agent);
                Long resolved = ticketRepository.countByAssignedToAndStatus(agent, "RESOLVED");
                Long openOrEscalated = ticketRepository.countByAssignedToAndStatusIn(agent, Arrays.asList("OPEN", "ESCALATED"));

                agentInfo.put("assignedCount", totalAssigned);
                agentInfo.put("assignedButNotResolved", openOrEscalated);
                agentInfo.put("resolvedCount", resolved);

                agentData.add(agentInfo);
            }

            agentData.sort((a, b) -> ((Long) b.get("resolvedCount")).compareTo((Long) a.get("resolvedCount")));

            int rank = 1;
            for (Map<String, Object> agentInfo : agentData) {
                agentInfo.put("rank", rank++);
            }

            return generateResponse("Agents fetched successfully", HttpStatus.OK, agentData);
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

            long ticketsAssigned = 0;
            long ticketsResolved = 0;
            long ticketsRaised = 0;
            long ticketsUnresolved = 0;

            boolean isAgentOrAdmin = user.getRole().stream()
                    .anyMatch(role -> role.getRole().equals("ROLE_AGENT") || role.getRole().equals("ROLE_ADMIN"));

            Map<String, Object> extraData = new HashMap<>();

            if (isAgentOrAdmin) {
                ticketsAssigned = ticketRepository.countByAssignedToAndStatusIn(user, Arrays.asList("OPEN", "ESCALATED"));
                ticketsResolved = ticketRepository.countByAssignedToAndStatus(user, "RESOLVED");
                extraData.put("ticketsAssigned", ticketsAssigned);
                extraData.put("ticketsResolved", ticketsResolved);
            } else {
                ticketsRaised = ticketRepository.countByCreatedBy(user);
                ticketsResolved = ticketRepository.countByCreatedByAndStatus(user, "RESOLVED");
                ticketsUnresolved = ticketRepository.countByCreatedByAndStatusNot(user, "RESOLVED");
                extraData.put("ticketsRaised", ticketsRaised);
//                extraData.put("tickets Resolved", ticketsResolved);// raised-unresolved check pannanum
                extraData.put("ticketsUnresolved", ticketsUnresolved);
                extraData.put("ticketsResolved", ticketsRaised-ticketsResolved);
            }

            Map<String, Object> finalResponse = new HashMap<>();
            finalResponse.put("user", dto);
            finalResponse.put("summary", extraData);

            return generateResponse("User profile fetched successfully", HttpStatus.OK, finalResponse);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse("Error fetching user profile: " + e.getMessage(), HttpStatus.NOT_FOUND, null);
        }
    }
}
