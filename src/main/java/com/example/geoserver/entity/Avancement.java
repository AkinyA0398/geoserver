package com.example.geoserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "avancement")
@Data
public class Avancement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @CreationTimestamp
    @Column(name = "date_reparation")
    private LocalDateTime dateReparation;
    
    @Column(name = "surface_reparee")
    private Double surfaceReparee;
    
    private String commentaire;
    
    @OneToOne
    @JoinColumn(name = "id_signalement")
    private Signalement signalement;
}