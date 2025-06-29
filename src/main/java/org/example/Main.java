package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.example.config.DatabaseConfig;
import org.example.config.ExceptionHandlerConfig;
import org.example.routes.AuthRoutes;
import org.example.routes.Router;
import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            DatabaseConfig.getConnection().close();
        } catch (SQLException e) {
            System.err.println("Error CRÍTICO al conectar con la base de datos al arrancar: " + e.getMessage());
            return;
        }

        ObjectMapper jacksonMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(jacksonMapper, false));
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> {
                it.reflectClientOrigin = true;
                it.allowCredentials = true;
                it.exposeHeader("Authorization");
            }));
        });

        List<Router> routers = List.of(new AuthRoutes());
        routers.forEach(router -> router.register(app));

        ExceptionHandlerConfig.register(app);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cerrando aplicación...");
            DatabaseConfig.close();
            app.stop();
        }));

        app.start(8080);

        System.out.println("Servidor iniciado en http://localhost:8080");
        System.out.println("Usuario inicial: admin / admin123");
    }
}