package com.example;

import com.example.migration.MigrationExecutor;
import com.example.service.DataInitializer;
import com.example.service.PerformanceReport;
import com.example.service.ReservationService;
import com.example.util.JpaUtil;

import java.time.LocalDateTime;

public final class App {
    private App() {
    }

    public static void main(String[] args) {
        try {
            JpaUtil.getEntityManagerFactory();
            new MigrationExecutor("jdbc:h2:mem:capstonedb;DB_CLOSE_DELAY=-1", "sa", "")
                    .executeMigration("db/migration/V2__reservation_search_index.sql");
            new DataInitializer().initData();
            ReservationService service = new ReservationService();
            System.out.println("Réservations par JPQL: " + service.rechercher("Salle A", LocalDateTime.now()).size());
            System.out.println("Réservations par Criteria: " + service.rechercherCriteria(null, LocalDateTime.now()).size());
            System.out.println(new PerformanceReport().generate());
        } finally {
            JpaUtil.close();
        }
    }
}
