package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.json.JavalinJackson;
import org.example.config.DatabaseConfig;
import org.example.config.ExceptionHandlerConfig;
import org.example.controllers.AuthController;
import org.example.daos.UserDAO;
import org.example.mappers.UserMapper;
import org.example.middlewares.AuthMiddleware;
import org.example.repositories.UserRepository;
import org.example.repositories.impl.UserRepositoryImpl;
import org.example.routes.AuthRoutes;
import org.example.routes.Router;
import org.example.services.AuthService;

import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<Router> routers = buildRouters();

        ObjectMapper jacksonMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        Javalin app = Javalin.create(config -> {
            config.jsonMapper(new JavalinJackson(jacksonMapper, false));
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> {
                it.reflectClientOrigin = true;
                it.allowCredentials = true;
                it.exposeHeader("Authorization");
            }));
        });

        routers.forEach(router -> router.register(app));
        ExceptionHandlerConfig.register(app);

        try {
            // Verificamos la conexión a la BD al inicio
            DatabaseConfig.getConnection().close();
        } catch (SQLException e) {
            System.err.println("Error CRÍTICO al conectar con la base de datos al arrancar: " + e.getMessage());
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Cerrando aplicación...");
            DatabaseConfig.close();
            app.stop();
        }));

        app.start(8080);

        System.out.println("Servidor iniciado en http://localhost:8080");
        System.out.println("Usuario inicial: admin / admin123");
    }

    /**
     * @return Una lista de routers configurados con sus dependencias.
     */
    private static List<Router> buildRouters() {
        // Capa de Datos
        final UserDAO userDAO = new UserDAO();
        final UserRepository userRepository = new UserRepositoryImpl(userDAO);

        // Mappers
        final UserMapper userMapper = new UserMapper();

        // Capa de Servicios
        final AuthService authService = new AuthService(userRepository, userMapper);

        // Middlewares
        final AuthMiddleware authMiddleware = new AuthMiddleware(authService);

        // Controladores
        final AuthController authController = new AuthController(authService);

        // Rutas
        return List.of(
                new AuthRoutes(authController, authMiddleware)
        );
    }
}