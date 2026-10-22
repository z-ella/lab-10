package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "reservations")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @Column(nullable = false)
    private LocalDateTime dateFin;

    @Column(length = 500)
    private String description;

    @Version
    private int version;

    protected Reservation() {
    }

    public Reservation(Salle salle, Utilisateur utilisateur, LocalDateTime dateDebut, LocalDateTime dateFin,
                       String description) {
        this.salle = Objects.requireNonNull(salle, "salle");
        this.utilisateur = Objects.requireNonNull(utilisateur, "utilisateur");
        this.dateDebut = Objects.requireNonNull(dateDebut, "dateDebut");
        this.dateFin = Objects.requireNonNull(dateFin, "dateFin");
        this.description = description;
        validateInterval();
    }

    public Long getId() {
        return id;
    }

    public Salle getSalle() {
        return salle;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public LocalDateTime getDateFin() {
        return dateFin;
    }

    public String getDescription() {
        return description;
    }

    public int getVersion() {
        return version;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private void validateInterval() {
        if (!dateFin.isAfter(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit suivre la date de debut.");
        }
    }
}
