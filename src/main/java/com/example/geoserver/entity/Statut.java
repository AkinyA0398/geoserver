package com.example.geoserver.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "statut")
@Data
public class Statut {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String nom;
}