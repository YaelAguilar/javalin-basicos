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
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(AppConfig.DB_URL);
            config.setUsername(AppConfig.DB_USER);
            config.setPassword(AppConfig.DB_PASSWORD);

            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000); // 30 segundos
            config.setIdleTimeout(600000);      // 10 minutos
            config.setMaxLifetime(1800000);     // 30 minutos
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);

            // Verificar la conexión y crear el esquema de la base de datos si es necesario.
            initDatabase();

        } catch (Exception e) {
            System.err.println("Error fatal inicializando la base de datos: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("No se pudo inicializar la conexión con la base de datos.", e);
        }
    }

    /**
     * Obtiene una conexión del pool de conexiones.
     * @return una conexión SQL.
     * @throws SQLException si hay un problema al obtener la conexión.
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("El pool de conexiones (DataSource) no está disponible o está cerrado.");
        }
        return dataSource.getConnection();
    }

    /**
     * Inicializa la estructura de la base de datos (tabla users) y crea un usuario inicial
     * si no existen. Este método es idempotente.
     */
    private static void initDatabase() {
        @Language("MySQL")
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                INDEX idx_username (username)
            ) ENGINE=InnoDB;
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            createInitialUser(conn);
        } catch (SQLException e) {
            System.err.println("Error al inicializar el esquema de la base de datos: " + e.getMessage());
            throw new RuntimeException("Error durante la inicialización de la BD.", e);
        }
    }

    /**
     * Verifica si el usuario 'admin' existe y, si no, lo crea.
     * @param conn La conexión a la base de datos a utilizar.
     * @throws SQLException si ocurre un error de base de datos.
     */
    private static void createInitialUser(Connection conn) throws SQLException {
        @Language("MySQL")
        String checkUserSQL = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSQL)) {
            checkStmt.setString(1, "admin");
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    // El usuario 'admin' no existe, así que lo creamos.
                    String hashedPassword = BCrypt.withDefaults().hashToString(12, "admin123".toCharArray());

                    @Language("MySQL")
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

    /**
     * Cierra el pool de conexiones de forma segura. Debe ser llamado al apagar la aplicación.
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Pool de conexiones cerrado correctamente.");
        }
    }
}