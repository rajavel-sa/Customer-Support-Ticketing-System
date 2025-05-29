package com.Eval_Task.CSTS.dto;

import com.Eval_Task.CSTS.model.Comment;

import java.time.LocalDateTime;

public class CommentResponseDTO {
    private Long id;
    private String content;
    private String username;
    private LocalDateTime createdAt;

    public CommentResponseDTO(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.username = comment.getUser().getUsername(); // Get inside transaction
        this.createdAt = comment.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
