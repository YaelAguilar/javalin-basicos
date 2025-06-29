package org.example.config;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/javalin_auth?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.setUsername("root");
        config.setPassword("password");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        dataSource = new HikariDataSource(config);

        // Crear tabla si no existe
        initDatabase();
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private static void initDatabase() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);

            // Insertar usuario inicial si no existe
            String checkUserSQL = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
            try (ResultSet rs = stmt.executeQuery(checkUserSQL)) {
                rs.next();

                if (rs.getInt(1) == 0) {
                    // Hash de la contraseña "admin123"
                    String hashedPassword = BCrypt.withDefaults()
                            .hashToString(12, "admin123".toCharArray());

                    String insertUserSQL = "INSERT INTO users (username, password) VALUES ('admin', ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertUserSQL)) {
                        pstmt.setString(1, hashedPassword);
                        pstmt.executeUpdate();
                    }

                    System.out.println("Usuario inicial creado: admin / admin123");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inicializando base de datos: " + e.getMessage());
        }
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}