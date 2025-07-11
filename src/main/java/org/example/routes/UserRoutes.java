package org.example.routes;

import io.javalin.Javalin;
import org.example.controllers.UserController;
import org.example.middlewares.AuthMiddleware;

public class UserRoutes implements RouteHandler {
    private final UserController userController;
    private final AuthMiddleware authMiddleware;

    public UserRoutes(UserController userController, AuthMiddleware authMiddleware) {
        this.userController = userController;
        this.authMiddleware = authMiddleware;
    }

    @Override
    public void register(Javalin app) {
        // --- Definición de Rutas con Seguridad Explícita ---
        // Eliminamos el wildcard '/*' para evitar ambigüedades.

        // Ruta para OBTENER TODOS los usuarios.
        // Requiere autenticación Y después requiere ser admin.
        app.before("/api/users", authMiddleware.requireAuth());
        app.before("/api/users", authMiddleware.requireAdmin());
        app.get("/api/users", userController::getAllUsers);

        // Ruta para REGISTRAR un usuario.
        // Requiere autenticación Y después requiere ser admin.
        app.before("/api/users/register", authMiddleware.requireAuth());
        app.before("/api/users/register", authMiddleware.requireAdmin());
        app.post("/api/users/register", userController::register);

        // Ruta para OBTENER el perfil propio.
        // Solo requiere autenticación.
        app.before("/api/users/profile", authMiddleware.requireAuth());
        app.get("/api/users/profile", userController::getProfile);

        // Ruta para OBTENER un usuario por ID.
        // Solo requiere autenticación.
        app.before("/api/users/{id}", authMiddleware.requireAuth());
        app.get("/api/users/{id}", userController::getUserById);
    }
}