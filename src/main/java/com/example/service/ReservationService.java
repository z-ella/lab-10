package com.example.service;

import com.example.model.Reservation;
import com.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class ReservationService {
    public Reservation creer(Long salleId, Long utilisateurId, LocalDateTime debut, LocalDateTime fin,
                             String description) {
        Objects.requireNonNull(salleId, "salleId");
        Objects.requireNonNull(utilisateurId, "utilisateurId");
        Objects.requireNonNull(debut, "debut");
        Objects.requireNonNull(fin, "fin");
        if (!fin.isAfter(debut)) {
            throw new IllegalArgumentException("La date de fin doit suivre la date de debut.");
        }

        return write(em -> {
            Long chevauchement = em.createQuery(
                            "SELECT COUNT(r) FROM Reservation r WHERE r.salle.id = :salleId " +
                                    "AND r.dateDebut < :fin AND r.dateFin > :debut", Long.class)
                    .setParameter("salleId", salleId)
                    .setParameter("debut", debut)
                    .setParameter("fin", fin)
                    .getSingleResult();
            if (chevauchement > 0) {
                throw new IllegalStateException("La salle est deja reservee pour ce creneau.");
            }
            Reservation reservation = new Reservation(
                    em.getReference(com.example.model.Salle.class, salleId),
                    em.getReference(com.example.model.Utilisateur.class, utilisateurId),
                    debut, fin, description);
            em.persist(reservation);
            return reservation;
        });
    }

    public Reservation trouverParId(Long id) {
        return read(em -> em.find(Reservation.class, id));
    }

    public List<Reservation> rechercher(String nomSalle, LocalDateTime debutApres) {
        return read(em -> em.createQuery(
                        "SELECT r FROM Reservation r WHERE (:nomSalle IS NULL OR r.salle.nom = :nomSalle) " +
                                "AND (:debutApres IS NULL OR r.dateDebut >= :debutApres) ORDER BY r.dateDebut",
                        Reservation.class)
                .setParameter("nomSalle", nomSalle)
                .setParameter("debutApres", debutApres)
                .getResultList());
    }

    public List<Reservation> rechercherCriteria(Long salleId, LocalDateTime debutApres) {
        return read(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Reservation> query = cb.createQuery(Reservation.class);
            Root<Reservation> reservation = query.from(Reservation.class);
            Predicate predicate = cb.conjunction();
            if (salleId != null) {
                predicate = cb.and(predicate, cb.equal(reservation.get("salle").get("id"), salleId));
            }
            if (debutApres != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(reservation.get("dateDebut"), debutApres));
            }
            return em.createQuery(query.select(reservation).where(predicate)
                    .orderBy(cb.asc(reservation.get("dateDebut")))).getResultList();
        });
    }

    private <T> T read(Work<T> work) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            return work.run(em);
        } finally {
            em.close();
        }
    }

    private <T> T write(Work<T> work) {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = work.run(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    @FunctionalInterface
    private interface Work<T> {
        T run(EntityManager em);
    }
}
