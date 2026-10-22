package com.example.migration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrationExecutor {
    private final String jdbcUrl;
    private final String username;
    private final String password;

    public MigrationExecutor(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    public void executeMigration(String resourcePath) {
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalArgumentException("Migration introuvable dans le classpath: " + resourcePath);
            }
            String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8).trim();
            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                 Statement statement = connection.createStatement()) {
                statement.execute(sql);
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Echec de la migration " + resourcePath, e);
        }
    }
}
