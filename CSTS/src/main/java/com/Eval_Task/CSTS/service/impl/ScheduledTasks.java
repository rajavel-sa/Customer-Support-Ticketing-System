package com.Eval_Task.CSTS.service.impl;

import com.Eval_Task.CSTS.model.Ticket;
import com.Eval_Task.CSTS.model.User;
import com.Eval_Task.CSTS.repository.TicketRepository;
import com.Eval_Task.CSTS.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

//@Service
//public class ScheduledTasks {
//
//    @Autowired private TicketRepository ticketRepository;
//    @Autowired private UserRepository userRepository;
//    @Autowired private JavaMailSender mailSender;
//
//    public final Set<Long> notifiedTicketIds = new HashSet<>();
//
//    @Scheduled(cron = "0 */2 * * * *")
//    public void escalateTickets() {
//        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(1);
//        List<Ticket> tickets = ticketRepository.findByStatusAndCreatedAtBefore("OPEN", cutoff);
//
//        for (Ticket ticket : tickets) {
//            ticket.setStatus("ESCALATED");
//            ticketRepository.save(ticket);
//        }
//
//        System.out.println("Escalated tickets not resolved within 1 minute.");
//    }
//
//    @Scheduled(cron = "0 * * * * *")
//    public void sendSummary() {
//        List<Ticket> escalatedTickets = ticketRepository.findByStatus("ESCALATED");
//
//        List<Ticket> newEscalated = escalatedTickets.stream()
//                .filter(ticket -> !notifiedTicketIds.contains(ticket.getId()))
//                .collect(Collectors.toList());
//
//        if (newEscalated.isEmpty()) {
//            System.out.println("No new escalated tickets. Skipping email notification.");
//            return;
//        }
//
//        List<User> admins = userRepository.findByRoleRole("ROLE_ADMIN");
//
//        StringBuilder summary = new StringBuilder("New escalated tickets:\n");
//        for (Ticket t : newEscalated) {
//            summary.append("Ticket ID: ").append(t.getId())
//                    .append(", Title: ").append(t.getTitle())
//                    .append(", Created At: ").append(t.getCreatedAt())
//                    .append("\n");
//            notifiedTicketIds.add(t.getId());
//        }
//
//        for (User admin : admins) {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setTo("sarajavel.others@gmail.com"); admin.getEmail()
//            message.setSubject("Escalated Ticket Summary");
//            message.setText(summary.toString());
//            mailSender.send(message);
//        }
//
//        System.out.println("Escalated ticket summary sent to admins.");
//    }
//}

@Service
public class ScheduledTasks {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private JavaMailSender mailSender;

    public final Set<Long> notifiedTicketIds = new HashSet<>();

    @Scheduled(cron = "0 0 */1 * * *")
    public void escalateTickets() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        List<Ticket> tickets = ticketRepository.findByStatusAndCreatedAtBefore("OPEN", cutoff);

        for (Ticket ticket : tickets) {
            ticket.setStatus("ESCALATED");
            ticketRepository.save(ticket);
        }

        System.out.println("Escalated tickets not resolved within 24 hours.");
    }


    @Scheduled(cron = "0 0 */1 * * *")
    public void sendSummary() {
        List<Ticket> escalatedTickets = ticketRepository.findByStatus("ESCALATED");

        List<Ticket> newEscalated = escalatedTickets.stream()
                .filter(ticket -> !notifiedTicketIds.contains(ticket.getId()))
                .collect(Collectors.toList());

        if (newEscalated.isEmpty()) {
            System.out.println("No new escalated tickets. Skipping email notification.");
            return;
        }

        List<User> admins = userRepository.findByRoleRole("ROLE_ADMIN");

        StringBuilder summary = new StringBuilder("New escalated tickets:\n");
        for (Ticket t : newEscalated) {
            summary.append("Ticket ID: ").append(t.getId())
                    .append(", Title: ").append(t.getTitle())
                    .append(", Created At: ").append(t.getCreatedAt())
                    .append("\n");
            notifiedTicketIds.add(t.getId());
        }

        for (User admin : admins) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(admin.getEmail());
            message.setSubject("Escalated Ticket Summary");
            message.setText(summary.toString());
            mailSender.send(message);
        }

        System.out.println("Escalated ticket summary sent to admins.");
    }
}
