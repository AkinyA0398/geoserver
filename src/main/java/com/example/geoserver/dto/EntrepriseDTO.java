package com.example.geoserver.dto;

import com.example.geoserver.entity.Entreprise;
import lombok.Data;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class EntrepriseDTO {
    private Long id;
    private String nom;
    private List<String> utilisateursIds;
    private List<Long> signalementsIds;

    public EntrepriseDTO() {}

    public EntrepriseDTO(Entreprise entreprise) {
        if (entreprise != null) {
            this.id = entreprise.getId();
            this.nom = entreprise.getNom();
            
            if (entreprise.getUtilisateurs() != null) {
                this.utilisateursIds = entreprise.getUtilisateurs().stream()
                    .map(utilisateur -> utilisateur.getId())
                    .collect(Collectors.toList());
            }
            
            if (entreprise.getSignalements() != null) {
                this.signalementsIds = entreprise.getSignalements().stream()
                    .map(signalement -> signalement.getId())
                    .collect(Collectors.toList());
            }
        }
    }
}