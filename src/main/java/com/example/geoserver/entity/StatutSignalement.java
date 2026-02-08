package com.example.geoserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "statut_signalement")
@Data
public class StatutSignalement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_signalement", nullable = false)
    private Signalement signalement;
    
    @ManyToOne
    @JoinColumn(name = "id_statut", nullable = false)
    private Statut statut;
    
    @CreationTimestamp
    @Column(name = "date_statut")
    private LocalDateTime dateStatut;
    
    private String commentaire;
}