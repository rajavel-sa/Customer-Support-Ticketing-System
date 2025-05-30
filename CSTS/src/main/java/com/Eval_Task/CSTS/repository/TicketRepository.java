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
    List<Ticket> findByStatusAndCreatedAtBefore(List<String> statuses, LocalDateTime cutoff);
    List<Ticket> findByStatusIn(List<String> open);
    List<Ticket> findByAssignedToAndStatusNot(User user, String resolved);
    long countByAssignedToAndStatusIn(User user, List<String> open);
    long countByAssignedToAndStatus(User user, String resolved);
    long countByCreatedBy(User user);
    long countByCreatedByAndStatusNot(User user, String resolved);
    long countByCreatedByAndStatus(User user, String resolved);
    Long countByAssignedTo(User agent);
}
