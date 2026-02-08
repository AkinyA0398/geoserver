package com.example.geoserver.dto;

import com.example.geoserver.entity.TypeSignalement;
import lombok.Data;

@Data
public class TypeSignalementDTO {
    private Long id;
    private String nom;
    private String icone;
    private String description;
    private String couleur;

    public TypeSignalementDTO() {}

    public TypeSignalementDTO(TypeSignalement typeSignalement) {
        if (typeSignalement != null) {
            this.id = typeSignalement.getId();
            this.nom = typeSignalement.getNom();
            this.icone = typeSignalement.getIcone();
            this.description = typeSignalement.getDescription();
            this.couleur = typeSignalement.getCouleur();
        }
    }
}