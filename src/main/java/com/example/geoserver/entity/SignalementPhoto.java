package com.example.geoserver.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "signalement_photo")
public class SignalementPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    @Column(nullable = false)
    private byte[] photo;

    @ManyToOne
    @JoinColumn(name = "id_signalement")
    private Signalement signalement;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }

    public Signalement getSignalement() { return signalement; }
    public void setSignalement(Signalement signalement) { this.signalement = signalement; }
}
