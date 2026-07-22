package com.netpoint.main.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Range;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "companyId", nullable = false)
    @ToString.Exclude
    private Company company;  // <-- mxolod es erti, saxeli company

    private String name;

    private String email;

    private String password;

    private String role;

    @Column(columnDefinition = "TEXT")
    private String profileImage;

    public enum Role {
        OWNER,
        ADMIN,
        CASHIER;

        public static boolean contains(String test) {
            if (test == null) return false;

            return Arrays.stream(Role.values())
                    .anyMatch(role -> role.name().equalsIgnoreCase(test));
        }
    }
}
