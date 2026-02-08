package com.example.geoserver.dto;

import com.example.geoserver.entity.Signalement;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignalementDTO {
    private Long id;
    private String description;
    private LocalDateTime dateCreation;
    private Double surface;
    private Double surfaceReparee;
    private Double budget;
    private PointDTO geom;
    private Integer severite;

    private UtilisateurDTO utilisateur;
    private EntrepriseDTO entreprise;
    private TypeSignalementDTO typeSignalement;
    private List<StatutDTO> statuts;
    private StatutDTO statutActuel;

    public SignalementDTO() {
    }

    public SignalementDTO(Signalement signalement) {
        if (signalement != null) {
            this.id = signalement.getId();
            this.description = signalement.getDescription();
            this.dateCreation = signalement.getDateCreation();
            this.surface = signalement.getSurface();
            this.budget = signalement.getBudget();
            this.geom = new PointDTO(signalement.getGeom());

            this.utilisateur = signalement.getUtilisateur() != null ? 
                                new UtilisateurDTO(signalement.getUtilisateur()) : null;
            this.entreprise = signalement.getEntreprise() != null ? 
                                new EntrepriseDTO(signalement.getEntreprise()) : null;
            this.typeSignalement = signalement.getTypeSignalement() != null
                    ? new TypeSignalementDTO(signalement.getTypeSignalement()) : null;

            if (signalement.getStatuts() != null) {
                this.statuts = signalement.getStatuts().stream()
                        .map(StatutDTO::new)
                        .collect(Collectors.toList());
            }

            this.statutActuel = signalement.getStatutActuel() != null ? 
                        new StatutDTO(signalement.getStatutActuel()) : null;
            if(statutActuel == null || statutActuel.getId() == 1) {
                this.surfaceReparee = 0.0; 
            } else if (statutActuel != null && statutActuel.getId() == 2 ) {
                this.surfaceReparee = this.surface * 0.5; 
            } else if (statutActuel != null && statutActuel.getId() == 3) {
                this.surfaceReparee = this.surface;
            }
        }
    }
}