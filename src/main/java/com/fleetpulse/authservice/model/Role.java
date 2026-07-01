package com.fleetpulse.authservice.model;

import com.fleetpulse.authservice.enums.TypeRole;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "role", schema = "iam")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", unique = true, nullable = false, length = 20)
    private TypeRole roleName;

    @OneToMany(mappedBy = "role")
    private List<Account> accounts = new ArrayList<>();
}