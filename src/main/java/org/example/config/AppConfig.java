package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .ignoreIfMissing()
            .load();

    public static final String DB_URL = getRequiredEnv("DB_URL");
    public static final String DB_USER = getRequiredEnv("DB_USER");
    public static final String DB_PASSWORD = getRequiredEnv("DB_PASSWORD");

    public static final String JWT_SECRET_KEY = getRequiredEnv("JWT_SECRET_KEY");

    /**
     * Obtiene una variable de entorno, primero del archivo .env y luego de las variables del sistema,
     * Lanza una excepción si la variable no se encuentra en ninguna de las dos fuentes,
     * @param key El nombre de la variable de entorno,
     * @return El valor de la variable.
     * @throws RuntimeException si la variable no está definida.
     */
    private static String getRequiredEnv(String key) {
        String value = dotenv.get(key);
        if (value == null) {
            value = System.getenv(key);
        }
        if (value == null) {
            System.err.println("Error: La variable de entorno requerida '" + key + "' no está definida.");
            System.err.println("Asegúrate de tener un archivo .env en la raíz del proyecto o de haberla exportado como variable de sistema.");
            throw new RuntimeException("Variable de entorno requerida no encontrada: " + key);
        }
        return value;
    }
}