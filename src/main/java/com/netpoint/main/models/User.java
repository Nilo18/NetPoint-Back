package com.netpoint.main.models;

import jakarta.persistence.*; // This imports @Entity, @Id, etc.
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Getter/Setter–ები ამ ორი ანოტაციით შევცვალე, შენც ესენი გამოიყენე ხოლმე, ან @Data
@Getter
@Setter
// default constructor ამით შევცვალე, შენც ეს გამოიყენე ხოლმე
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    private String email;
    // companyId ია დასამატებელი რო ყველა იუზერი შესაბამის კომპანიასთან დაკავშირდეს
}