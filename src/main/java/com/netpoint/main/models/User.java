package com.netpoint.main.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users") // Maps this class to the 'users' table in Supabase
@NoArgsConstructor    // Required by Hibernate
@AllArgsConstructor   // Useful for creating users in your code
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-increments the ID
    private int id;

    private int companyId;
    private String name;
    private String email;
    private String password;
}