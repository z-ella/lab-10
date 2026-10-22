# Lab 10 — Room reservation capstone

This Java 11 Maven application combines a normalized JPA model (`Salle`, `Utilisateur`, `Reservation`), resource-local transactions, `@Version` optimistic locking, Hibernate's second-level cache, JPQL and Criteria API searches, and an idempotent schema-index migration.

## Run

```sh
mvn clean test
mvn exec:java
```

The demo runs the migration, initializes realistic sample data, exercises both query styles, and prints Hibernate query, SQL-statement, and cache-hit statistics. Tests cover creation, search, overlapping bookings, adjacent slots, and stale concurrent updates.
