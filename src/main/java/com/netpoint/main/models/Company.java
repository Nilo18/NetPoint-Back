package com.netpoint.main.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Entity
@NoArgsConstructor  // obieqtis shesaqmnelad schirdeba hibernates
@AllArgsConstructor // Useful for your own code
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // aq finalebit gqonda eseni, maagram movashore tore ver vaketebdi
    private String name;
    private String email;
    private String password;
    private String industry;
    @OneToMany(mappedBy = "companyId", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<User> userList;
}