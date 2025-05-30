package com.Eval_Task.CSTS.repository;

import com.Eval_Task.CSTS.model.Comment;
import com.Eval_Task.CSTS.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {}
