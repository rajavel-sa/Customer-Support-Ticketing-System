package com.Eval_Task.CSTS.dto;

public class TicketRequest {
    public String title;
    public String description;

    public TicketRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public TicketRequest() {
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

    public void setStatus(String open) {

    }
}
