package com.Eval_Task.CSTS.repository;

import com.Eval_Task.CSTS.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    List<User> findByRoleRole(String role);

    @Query("SELECT u FROM User u JOIN u.role r WHERE r.role = 'ROLE_AGENT'")
    List<User> findAllAgents();
}