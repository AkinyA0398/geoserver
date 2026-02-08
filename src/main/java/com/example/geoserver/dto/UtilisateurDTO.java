package com.example.geoserver.dto;

import com.example.geoserver.entity.Utilisateur;
import lombok.Data;

@Data
public class UtilisateurDTO {
    private String id;
    private String email;
    private String nom;
    private Integer attempts;
    private Boolean blocked;

    public UtilisateurDTO() {}

    public UtilisateurDTO(Utilisateur utilisateur) {
        if (utilisateur != null) {
            this.id = utilisateur.getId();
            this.email = utilisateur.getEmail();
            this.nom = utilisateur.getNom();
            this.attempts = utilisateur.getAttempts();
            this.blocked = utilisateur.getBlocked();
        }
    }
}