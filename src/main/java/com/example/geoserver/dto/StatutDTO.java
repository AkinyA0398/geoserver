package com.example.geoserver.dto;

import java.time.LocalDateTime;

import com.example.geoserver.entity.Statut;
import com.example.geoserver.entity.StatutSignalement;

import lombok.Data;

@Data
public class StatutDTO {
    private Long id;
    private String nom;
    private LocalDateTime dateStatut;
    private String commentaire;

    public StatutDTO() {}

    public StatutDTO(Statut statut) {
        if (statut != null) {
            this.id = statut.getId();
            this.nom = statut.getNom();
        }
    }

    
    public StatutDTO(StatutSignalement statutSignalement) {
        if (statutSignalement != null) {
            this.id = statutSignalement.getStatut() != null ? 
                        statutSignalement.getStatut().getId() : null;
            this.nom = statutSignalement.getStatut() != null ? 
                        statutSignalement.getStatut().getNom() : null;
            this.dateStatut = statutSignalement.getDateStatut();
            this.commentaire = statutSignalement.getCommentaire();
        }
    }
}