package com.Eval_Task.CSTS.service.impl;

import com.Eval_Task.CSTS.model.Role;
import com.Eval_Task.CSTS.model.Ticket;
import com.Eval_Task.CSTS.model.User;
import com.Eval_Task.CSTS.repository.TicketRepository;
import com.Eval_Task.CSTS.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduledTasksTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private ScheduledTasks scheduledTasks;

    private Ticket ticket;
    private User admin;

    @BeforeEach
    void setUp() {
        ticket = new Ticket();
        ticket.setId(1L);
        ticket.setTitle("Test Ticket");
        ticket.setStatus("OPEN");
        ticket.setCreatedAt(LocalDateTime.now().minusMinutes(2));

        admin = new User();
        admin.setEmail("admin@example.com");
        admin.setRole(new Role("ROLE_ADMIN"));

        scheduledTasks.notifiedTicketIds.clear();
    }

    @Test
    void escalateTickets_shouldEscalateOldOpenOrAssignedTickets() {
        when(ticketRepository.findByStatusAndCreatedAtBefore(eq(Arrays.asList("OPEN", "ASSIGNED")), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(ticket));
        when(ticketRepository.save(any(Ticket.class))).thenReturn(ticket);

        scheduledTasks.escalateTickets();

        assertEquals("ESCALATED", ticket.getStatus());
        verify(ticketRepository).save(ticket);
        verify(ticketRepository).findByStatusAndCreatedAtBefore(eq(Arrays.asList("OPEN", "ASSIGNED")), any(LocalDateTime.class));
    }

    @Test
    void escalateTickets_noMatchingTickets_shouldDoNothing() {
        when(ticketRepository.findByStatusAndCreatedAtBefore(eq(Arrays.asList("OPEN", "ASSIGNED")), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        scheduledTasks.escalateTickets();

        verify(ticketRepository, never()).save(any());
        verify(ticketRepository).findByStatusAndCreatedAtBefore(eq(Arrays.asList("OPEN", "ASSIGNED")), any(LocalDateTime.class));
    }

    @Test
    void sendSummaryToAdmins_newEscalatedTickets_shouldSendEmails() {
        ticket.setStatus("ESCALATED");
        when(ticketRepository.findByStatus("ESCALATED")).thenReturn(Arrays.asList(ticket));
        when(userRepository.findByRoleRole("ROLE_ADMIN")).thenReturn(Arrays.asList(admin));

        scheduledTasks.sendSummary();

        verify(mailSender).send(argThat((SimpleMailMessage message) -> {
            assertEquals("sarajavel.others@gmail.com", message.getTo()[0]);
            assertEquals("Escalated Ticket Summary", message.getSubject());
            assertTrue(message.getText().contains("Ticket ID: 1, Title: Test Ticket"));
            return true;
        }));
        assertEquals(1, scheduledTasks.notifiedTicketIds.size());
        assertTrue(scheduledTasks.notifiedTicketIds.contains(1L));
    }

    @Test
    void sendSummaryToAdmins_noNewEscalatedTickets_shouldSkipEmails() {
        scheduledTasks.notifiedTicketIds.add(1L);
        ticket.setStatus("ESCALATED");
        when(ticketRepository.findByStatus("ESCALATED")).thenReturn(Arrays.asList(ticket));

        scheduledTasks.sendSummary();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    void sendSummaryToAdmins_noAdmins_shouldSkipEmails() {
        ticket.setStatus("ESCALATED");
        when(ticketRepository.findByStatus("ESCALATED")).thenReturn(Arrays.asList(ticket));
        when(userRepository.findByRoleRole("ROLE_ADMIN")).thenReturn(Collections.emptyList());

        scheduledTasks.sendSummary();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        assertEquals(1, scheduledTasks.notifiedTicketIds.size());
        assertTrue(scheduledTasks.notifiedTicketIds.contains(1L));
    }

    @Test
    void sendSummaryToAdmins_noEscalatedTickets_shouldSkipEmails() {
        when(ticketRepository.findByStatus("ESCALATED")).thenReturn(Collections.emptyList());

        scheduledTasks.sendSummary();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
        verifyNoInteractions(userRepository);
        assertEquals(0, scheduledTasks.notifiedTicketIds.size());
    }
}