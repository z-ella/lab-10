package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "utilisateurs")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Column(nullable = false, unique = true)
    private String email;

    protected Utilisateur() {
    }

    public Utilisateur(String nom, String email) {
        this.nom = Objects.requireNonNull(nom, "nom");
        this.email = Objects.requireNonNull(email, "email");
    }

    public Long getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }
}
