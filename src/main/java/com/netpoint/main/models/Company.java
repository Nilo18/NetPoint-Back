package com.netpoint.main.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private final int id;
    private final String name;
    private final String email;
    private final String password;
    private final String industry;
}
