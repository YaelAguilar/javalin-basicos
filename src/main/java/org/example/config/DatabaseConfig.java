package org.example.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.intellij.lang.annotations.Language;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static volatile HikariDataSource dataSource;

    static {
        initializeDataSource();
    }

    private static void initializeDataSource() {
        try {
            HikariConfig config = createHikariConfig();
            dataSource = new HikariDataSource(config);
            initDatabase();
        } catch (Exception e) {
            System.err.println("Error fatal al inicializar la base de datos: " + e.getMessage());
            throw new RuntimeException("No se pudo inicializar la conexión con la base de datos.", e);
        }
    }

    private static HikariConfig createHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(AppConfig.DB_URL);
        config.setUsername(AppConfig.DB_USER);
        config.setPassword(AppConfig.DB_PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return config;
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("El pool de conexiones (DataSource) no está disponible o está cerrado.");
        }
        return dataSource.getConnection();
    }

    private static void initDatabase() {
        @Language("MySQL")
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, password VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, INDEX idx_username (username)) ENGINE=InnoDB;";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            createInitialUser(conn);
        } catch (SQLException e) {
            System.err.println("Error al inicializar el esquema de la base de datos: " + e.getMessage());
            throw new RuntimeException("Error durante la inicialización de la BD.", e);
        }
    }

    private static void createInitialUser(Connection conn) throws SQLException {
        String checkUserSQL = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSQL)) {
            checkStmt.setString(1, "admin");
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String hashedPassword = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());
                    String insertUserSQL = "INSERT INTO users (username, password) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSQL)) {
                        insertStmt.setString(1, "admin");
                        insertStmt.setString(2, hashedPassword);
                        insertStmt.executeUpdate();
                        System.out.println("Usuario inicial 'admin' creado con contraseña 'admin123'.");
                    }
                }
            }
        }
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Pool de conexiones cerrado correctamente.");
        }
    }
}