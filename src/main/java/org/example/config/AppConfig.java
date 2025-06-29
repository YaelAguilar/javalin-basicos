package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.example.Main; // Importamos Main para acceder a la bandera 'isTesting'

public class AppConfig {
    
    // Lo hacemos no final y lo inicializamos como null.
    private static Dotenv dotenv;

    // Método "lazy-loader" sincronizado para ser seguro en cualquier entorno.
    private static synchronized Dotenv getDotenv() {
        if (dotenv == null) {
            String filename = Main.isTesting ? ".env.test" : ".env";
            System.out.println("LAZY LOADING a partir de: " + filename);
            dotenv = Dotenv.configure()
                    .filename(filename) // Carga el archivo correcto
                    .directory("./")
                    .ignoreIfMissing()
                    .load();
        }
        return dotenv;
    }

    // Ahora las variables son métodos que usan el lazy-loader.
    public static String getDbUrl() {
        return getRequiredEnv("DB_URL");
    }

    public static String getDbUser() {
        return getRequiredEnv("DB_USER");
    }
    
    public static String getDbPassword() {
        return getRequiredEnv("DB_PASSWORD");
    }

    public static String getJwtSecretKey() {
        return getRequiredEnv("JWT_SECRET_KEY");
    }

    private static String getRequiredEnv(String key) {
        // Obtenemos la instancia de Dotenv a través de nuestro loader perezoso.
        String value = getDotenv().get(key);
        
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            System.err.println("Error: La variable de entorno requerida '" + key + "' no está definida.");
            System.err.println("Asegúrate de tener el archivo .env o .env.test adecuado en la raíz del proyecto.");
            throw new RuntimeException("Variable de entorno requerida no encontrada: " + key);
        }
        return value;
    }
}