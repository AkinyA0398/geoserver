package com.example.geoserver.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "type_signalement")
@Data
public class TypeSignalement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nom;
    
    @Column(name = "icone")
    private String icone; 
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "couleur", length = 7) 
    private String couleur = "#3498db";
}