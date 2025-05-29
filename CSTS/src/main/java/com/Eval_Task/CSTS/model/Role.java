package com.Eval_Task.CSTS.model;

import javax.persistence.*;

@Entity
@Table(name="a0_roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String role;


    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }

    public Role() {}
    public Role(String role) {
        this.role = role;
    }


}