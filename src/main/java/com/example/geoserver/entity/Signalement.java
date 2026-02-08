package com.example.geoserver.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;

@Entity
@Table(name = "signalement")
@Data
public class Signalement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @CreationTimestamp
    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    private Double surface;
    private Double budget;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point geom;

    @Column(name = "severite")
    private Integer severite = 1;

    @ManyToOne
    @JoinColumn(name = "id_utilisateur")
    private Utilisateur utilisateur;

    @ManyToOne
    @JoinColumn(name = "id_entreprise")
    private Entreprise entreprise;

    @ManyToOne
    @JoinColumn(name = "id_type_signalement")
    private TypeSignalement typeSignalement;

    @OneToMany(mappedBy = "signalement", cascade = CascadeType.ALL)
    private List<Avancement> avancements;

    @OneToMany(mappedBy = "signalement", cascade = CascadeType.ALL)
    private List<StatutSignalement> statuts;

    public StatutSignalement getStatutActuel() {
        if (statuts == null || statuts.isEmpty())
            return null;
        return statuts.stream()
                .max(Comparator.comparing(StatutSignalement::getDateStatut))
                .orElse(null);
    }

}