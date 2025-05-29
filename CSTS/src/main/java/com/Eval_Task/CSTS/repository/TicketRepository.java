package com.Eval_Task.CSTS.repository;

import com.Eval_Task.CSTS.model.Ticket;
import com.Eval_Task.CSTS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByCreatedBy(User user);
    List<Ticket> findByStatus(String status);
    List<Ticket> findByAssignedTo(User user);
    long countByStatus(String open);
    List<Ticket> findByStatusAndCreatedAtBefore(String open, LocalDateTime cutoff);
    List<Ticket> findByStatusIn(List<String> open);
}
