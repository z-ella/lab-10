package com.example.service;

import com.example.model.Reservation;
import com.example.util.JpaUtil;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class PerformanceReport {
    public String generate() {
        SessionFactory sessionFactory = JpaUtil.getEntityManagerFactory().unwrap(SessionFactory.class);
        Statistics statistics = sessionFactory.getStatistics();
        statistics.clear();
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            List<Reservation> reservations = em.createQuery(
                            "SELECT r FROM Reservation r WHERE r.salle.nom = :nomSalle AND r.dateDebut >= :date " +
                                    "ORDER BY r.dateDebut", Reservation.class)
                    .setParameter("nomSalle", "Salle A")
                    .setParameter("date", LocalDateTime.now())
                    .getResultList();
            long queries = statistics.getQueryExecutionCount();
            long statements = statistics.getPrepareStatementCount();
            long cacheHits = statistics.getSecondLevelCacheHitCount();
            return String.format("Reservations trouvees: %d | requetes JPQL: %d | instructions SQL: %d | hits cache: %d",
                    reservations.size(), queries, statements, cacheHits);
        } finally {
            em.close();
        }
    }
}
