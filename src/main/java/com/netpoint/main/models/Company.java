package com.netpoint.main.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Setter;

@Data
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final Integer id;
    @NotBlank
    private final String name;
    @NotBlank
    @Email
    private final String email;
    @NotBlank
    @Size(min = 8)
    private String password;
    @NotBlank
    private final String industry;
}
