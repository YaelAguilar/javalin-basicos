package org.example.controllers;

import org.example.services.AuthService;
import org.example.utils.JWTUtil;
import io.javalin.http.Context;
import java.util.Map;

public class AuthController {
    private final AuthService authService;

    public AuthController() {
        this.authService = new AuthService();
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public void login(Context ctx) {
        try {
            // Obtener datos del request body
            Map<String, Object> requestBody = ctx.bodyAsClass(Map.class);
            String username = (String) requestBody.get("username");
            String password = (String) requestBody.get("password");

            // Realizar login
            AuthService.LoginResult result = authService.login(username, password);

            if (result.isSuccess()) {
                ctx.status(200).json(Map.of(
                        "success", true,
                        "message", result.getMessage(),
                        "token", result.getToken(),
                        "user", Map.of(
                                "id", result.getUser().getId(),
                                "username", result.getUser().getUsername()
                        )
                ));
            } else {
                ctx.status(401).json(Map.of(
                        "success", false,
                        "message", result.getMessage()
                ));
            }

        } catch (Exception e) {
            ctx.status(400).json(Map.of(
                    "success", false,
                    "message", "Error en el formato de la petición: " + e.getMessage()
            ));
        }
    }

    public void logout(Context ctx) {
        try {
            String authHeader = ctx.header("Authorization");
            String token = JWTUtil.extractTokenFromHeader(authHeader);

            AuthService.LogoutResult result = authService.logout(token);

            if (result.isSuccess()) {
                ctx.status(200).json(Map.of(
                        "success", true,
                        "message", result.getMessage()
                ));
            } else {
                ctx.status(400).json(Map.of(
                        "success", false,
                        "message", result.getMessage()
                ));
            }

        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }

    public void profile(Context ctx) {
        try {
            String authHeader = ctx.header("Authorization");
            String token = JWTUtil.extractTokenFromHeader(authHeader);

            if (token != null && authService.isTokenValid(token)) {
                String username = authService.extractUsernameFromToken(token);
                ctx.status(200).json(Map.of(
                        "success", true,
                        "message", "Perfil obtenido exitosamente",
                        "username", username
                ));
            } else {
                ctx.status(401).json(Map.of(
                        "success", false,
                        "message", "Token inválido o expirado"
                ));
            }

        } catch (Exception e) {
            ctx.status(500).json(Map.of(
                    "success", false,
                    "message", "Error interno del servidor: " + e.getMessage()
            ));
        }
    }
}