package com.Eval_Task.CSTS.service.impl;

import com.Eval_Task.CSTS.dto.CommentRequest;
import com.Eval_Task.CSTS.dto.CommentResponseDTO;
import com.Eval_Task.CSTS.dto.TicketRequest;
import com.Eval_Task.CSTS.dto.TicketResponseDTO;
import com.Eval_Task.CSTS.model.*;
import com.Eval_Task.CSTS.repository.*;
import com.Eval_Task.CSTS.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketServiceImpl implements TicketService {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CommentRepository commentRepository;

    @Transactional
    @Override
    public ResponseEntity<Object> createTicket(TicketRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User createdBy = userRepository.findByUsername(username);

            Ticket ticket = new Ticket();
            ticket.setTitle(request.getTitle());
            ticket.setDescription(request.getDescription());
            ticket.setStatus("OPEN");
            ticket.setCreatedBy(createdBy);
            ticket.setCreatedAt(LocalDateTime.now());

            Ticket savedTicket = ticketRepository.save(ticket);
            TicketResponseDTO responseDTO = new TicketResponseDTO(savedTicket);
            return generateResponse("Ticket created successfully", HttpStatus.CREATED, responseDTO);
        } catch (Exception e) {
            return generateResponse("Error creating ticket: " + e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Object> getMyTickets() {
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        List<Ticket> tickets;
        if (user.getRole().stream().anyMatch(role -> role.getRole().equals("ROLE_CUSTOMER"))) {
            tickets = ticketRepository.findByCreatedBy(user);
        } else {
            tickets = ticketRepository.findByAssignedTo(user);
        }

        List<TicketResponseDTO> response = tickets.stream()
                .map(TicketResponseDTO::new)
                .collect(Collectors.toList());

        return generateResponse("Tickets fetched successfully", HttpStatus.OK, response);
    } catch (Exception e) {
        e.printStackTrace();
        return generateResponse("Error fetching tickets: " + e.getMessage(), HttpStatus.BAD_REQUEST, null);
    }
}

    @Transactional
    @Override
    public ResponseEntity<Object> getOpenTickets() {
        try {
            List<Ticket> openTickets = ticketRepository.findByStatusIn(Arrays.asList("OPEN", "ESCALATED"));

            List<TicketResponseDTO> response = openTickets.stream()
                    .map(TicketResponseDTO::new)
                    .collect(Collectors.toList());

            return generateResponse("Success", HttpStatus.OK, response);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse("Error", HttpStatus.BAD_REQUEST, null);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Object> assignTicket(Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User assignedTo = userRepository.findByUsername(username);

            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            if (!"OPEN".equals(ticket.getStatus())) {
                return generateResponse("Only OPEN tickets can be assigned", HttpStatus.BAD_REQUEST, null);
            }

            ticket.setAssignedTo(assignedTo);
            ticket.setStatus("ASSIGNED");
            ticket.setUpdatedAt(LocalDateTime.now());

            Ticket updatedTicket = ticketRepository.save(ticket);
            TicketResponseDTO dto = new TicketResponseDTO(updatedTicket);
            return generateResponse("Ticket assigned successfully", HttpStatus.OK, dto);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse("Error assigning ticket: " + e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Object> resolveTicket(Long id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User currentUser = userRepository.findByUsername(username);

            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            boolean isAssignedAgent = currentUser.equals(ticket.getAssignedTo());
            boolean isAdmin = currentUser.getRole().stream().anyMatch(role -> role.getRole().equals("ROLE_ADMIN"));

            if (!isAssignedAgent && !isAdmin) {
                return generateResponse("Only assigned agent or admin can resolve tickets", HttpStatus.FORBIDDEN, null);
            }

            ticket.setStatus("RESOLVED");
            ticket.setResolvedAt(LocalDateTime.now());
            ticket.setUpdatedAt(LocalDateTime.now());

            Ticket updatedTicket = ticketRepository.save(ticket);
            TicketResponseDTO dto = new TicketResponseDTO(updatedTicket);
            return generateResponse("Ticket resolved successfully", HttpStatus.OK, dto);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse("Error resolving ticket: " + e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    @Transactional
    @Override
    public ResponseEntity<Object> addComment(Long id, CommentRequest comment) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userRepository.findByUsername(username);

            Ticket ticket = ticketRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Ticket not found"));

            Comment newComment = new Comment();
            newComment.setContent(comment.getContent());
            newComment.setTicket(ticket);
            newComment.setUser(user);
            newComment.setCreatedAt(LocalDateTime.now());

            Comment savedComment = commentRepository.save(newComment);

//            savedComment = commentRepository.findById(savedComment.getId())
//                    .orElseThrow(() -> new RuntimeException("Comment fetch failed"));

            ticket.setUpdatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);

            CommentResponseDTO dto = new CommentResponseDTO(savedComment);
            return generateResponse("Comment added successfully", HttpStatus.CREATED, dto);
        } catch (Exception e) {
            e.printStackTrace();
            return generateResponse("Error adding comment: " + e.getMessage(), HttpStatus.BAD_REQUEST, null);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////

    private ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object responseObj) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status.value());
        map.put("data", responseObj);
        return new ResponseEntity<>(map, status);
    }
}