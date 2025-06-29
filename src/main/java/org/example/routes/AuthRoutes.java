package org.example.routes;

import io.javalin.Javalin;
import org.example.controllers.AuthController;
import org.example.middlewares.AuthMiddleware;

public class AuthRoutes implements Router {
    private final AuthController authController;
    private final AuthMiddleware authMiddleware;

    public AuthRoutes() {
        this.authController = new AuthController();
        this.authMiddleware = new AuthMiddleware();
    }

    @Override
    public void register(Javalin app) {
        app.post("/api/auth/login", authController::login);

        app.before("/api/auth/logout", authMiddleware.requireAuth);
        app.before("/api/auth/profile", authMiddleware.requireAuth);

        app.post("/api/auth/logout", authController::logout);
        app.get("/api/auth/profile", authController::profile);
    }
}