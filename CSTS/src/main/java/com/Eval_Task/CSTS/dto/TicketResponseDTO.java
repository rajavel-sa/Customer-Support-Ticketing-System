package com.Eval_Task.CSTS.dto;

import com.Eval_Task.CSTS.model.Ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

//public class TicketResponseDTO {
//    private Long id;
//    private String title;
//    private String status;
//    // other simple fields
//
//    // constructor from Ticket entity
//    public TicketResponseDTO(Ticket ticket) {
//        this.id = ticket.getId();
//        this.title = ticket.getTitle();
//        this.status = ticket.getStatus();
//        // don't include lazy-loaded relationships
//    }
//}

public class TicketResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String createdBy;
    private String assignedTo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private List<CommentResponseDTO> comments;
    public TicketResponseDTO(Ticket ticket) {
        this.id = ticket.getId();
        this.title = ticket.getTitle();
        this.description = ticket.getDescription();
        this.status = ticket.getStatus();
        this.createdBy = ticket.getCreatedBy().getUsername();  // Access inside service when session is open
        this.assignedTo = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUsername() : null;
        this.createdAt = ticket.getCreatedAt();
        this.updatedAt = ticket.getUpdatedAt();
        this.resolvedAt = ticket.getResolvedAt();

        this.comments = ticket.getComments().stream()
                .map(CommentResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<CommentResponseDTO> getComments() {
        return comments;
    }

    public void setComments(List<CommentResponseDTO> comments) {
        this.comments = comments;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
