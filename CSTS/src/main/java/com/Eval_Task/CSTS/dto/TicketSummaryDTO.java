package com.Eval_Task.CSTS.dto;

public class TicketSummaryDTO {
    private long totalTickets;
    private long openTickets;
    private long assignedTickets;
    private long resolvedTickets;

    public TicketSummaryDTO(long total, long open, long assigned, long resolved) {
        this.totalTickets = total;
        this.openTickets = open;
        this.assignedTickets = assigned;
        this.resolvedTickets = resolved;
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getAssignedTickets() {
        return assignedTickets;
    }

    public void setAssignedTickets(long assignedTickets) {
        this.assignedTickets = assignedTickets;
    }
}