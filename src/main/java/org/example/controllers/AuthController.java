package org.example.controllers;

import io.javalin.http.Context;
import org.example.dtos.auth.LoginRequest;
import org.example.dtos.auth.LoginResponse;
import org.example.services.AuthService;
import java.util.Map;

public class AuthController {
    private final AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    public void login(Context ctx) {
        LoginRequest loginRequest = ctx.bodyAsClass(LoginRequest.class);
        LoginResponse loginResponse = authService.login(loginRequest);
        ctx.status(200).json(Map.of(
                "success", true,
                "message", "Login exitoso",
                "data", loginResponse
        ));
    }

    public void logout(Context ctx) {
        String token = ctx.attribute("token");
        authService.logout(token);
        ctx.status(200).json(Map.of("success", true, "message", "Logout exitoso"));
    }

    public void profile(Context ctx) {
        String username = ctx.attribute("username");

        if (username == null) {
            throw new IllegalStateException("El username no debería ser nulo en una ruta protegida.");
        }

        ctx.status(200).json(Map.of(
                "success", true,
                "message", "Perfil obtenido exitosamente",
                "data", Map.of("username", username)
        ));
    }
}