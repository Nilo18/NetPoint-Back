package com.netpoint.main.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
    private Integer id;
//    @NotNull
    private int companyId;
//    @NotNull
    private String name;
//    @NotNull
    private String email;
//    @NotNull
    private String password;
}