package com.example.service;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import com.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;

public class DataInitializer {
    public void initData() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (em.createQuery("SELECT COUNT(s) FROM Salle s", Long.class).getSingleResult() == 0) {
                Salle salleA = new Salle("Salle A", 20);
                Salle salleB = new Salle("Salle B", 12);
                Utilisateur alice = new Utilisateur("Alice Dupont", "alice.dupont@example.com");
                Utilisateur bob = new Utilisateur("Bob Martin", "bob.martin@example.com");
                em.persist(salleA);
                em.persist(salleB);
                em.persist(alice);
                em.persist(bob);
                LocalDateTime start = LocalDateTime.now().plusDays(1).withMinute(0).withSecond(0).withNano(0);
                em.persist(new Reservation(salleA, alice, start, start.plusHours(2), "Atelier de planification"));
                em.persist(new Reservation(salleB, bob, start.plusHours(2), start.plusHours(3), "Revue d'equipe"));
            }
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
