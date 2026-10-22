package com.example;

import com.example.migration.MigrationExecutor;
import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import com.example.service.ReservationService;
import com.example.util.JpaUtil;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReservationCapstoneTest {
    private final ReservationService service = new ReservationService();
    private Long salleId;
    private Long utilisateurId;
    private final LocalDateTime start = LocalDateTime.of(2032, 5, 1, 9, 0);

    @Before
    public void prepareDatabase() {
        EntityManager em = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Reservation").executeUpdate();
            em.createQuery("DELETE FROM Utilisateur").executeUpdate();
            em.createQuery("DELETE FROM Salle").executeUpdate();
            Salle salle = new Salle("Salle test", 10);
            Utilisateur utilisateur = new Utilisateur("Test User", "test@example.com");
            em.persist(salle);
            em.persist(utilisateur);
            tx.commit();
            salleId = salle.getId();
            utilisateurId = utilisateur.getId();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
        JpaUtil.getEntityManagerFactory().getCache().evictAll();
    }

    @AfterClass
    public static void closeFactory() {
        JpaUtil.close();
    }

    @Test
    public void createsReservationAndQueriesWithJpqlAndCriteria() {
        Reservation saved = service.creer(salleId, utilisateurId, start, start.plusHours(1), "Review");
        assertNotNull(saved.getId());
        assertEquals(1, service.rechercher("Salle test", start).size());
        List<Reservation> criteria = service.rechercherCriteria(salleId, start);
        assertEquals(1, criteria.size());
        assertEquals("Review", service.trouverParId(saved.getId()).getDescription());
    }

    @Test
    public void rejectsOverlappingBookingButAllowsAdjacentSlots() {
        service.creer(salleId, utilisateurId, start, start.plusHours(1), "First");
        try {
            service.creer(salleId, utilisateurId, start.plusMinutes(30), start.plusHours(2), "Overlap");
            fail("Overlapping room reservations must be rejected.");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("deja reservee"));
        }
        assertNotNull(service.creer(salleId, utilisateurId, start.plusHours(1), start.plusHours(2), "Next"));
    }

    @Test
    public void optimisticVersionPreventsStaleReservationUpdate() {
        Reservation saved = service.creer(salleId, utilisateurId, start, start.plusHours(1), "Original");
        EntityManager first = JpaUtil.getEntityManagerFactory().createEntityManager();
        EntityManager second = JpaUtil.getEntityManagerFactory().createEntityManager();
        try {
            Reservation current = first.find(Reservation.class, saved.getId());
            Reservation stale = second.find(Reservation.class, saved.getId());
            first.getTransaction().begin();
            current.setDescription("Updated");
            first.getTransaction().commit();
            second.getTransaction().begin();
            stale.setDescription("Stale write");
            try {
                second.getTransaction().commit();
                fail("A stale update should be rejected.");
            } catch (javax.persistence.RollbackException expected) {
                assertTrue(hasCause(expected, javax.persistence.OptimisticLockException.class));
                if (second.getTransaction().isActive()) {
                    second.getTransaction().rollback();
                }
            }
        } finally {
            first.close();
            second.close();
        }
        assertEquals("Updated", service.trouverParId(saved.getId()).getDescription());
    }

    @Test
    public void schemaIndexMigrationIsIdempotent() {
        MigrationExecutor migration = new MigrationExecutor("jdbc:h2:mem:capstonedb;DB_CLOSE_DELAY=-1", "sa", "");
        migration.executeMigration("db/migration/V2__reservation_search_index.sql");
        migration.executeMigration("db/migration/V2__reservation_search_index.sql");
    }

    private boolean hasCause(Throwable error, Class<? extends Throwable> expectedType) {
        for (Throwable current = error; current != null; current = current.getCause()) {
            if (expectedType.isInstance(current)) {
                return true;
            }
        }
        return false;
    }
}
