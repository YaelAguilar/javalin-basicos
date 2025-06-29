package org.example.middlewares;

import org.example.services.AuthService;
import org.example.utils.JWTUtil;
import io.javalin.http.Handler;
import java.util.Map;

public class AuthMiddleware {
    private final AuthService authService;

    public AuthMiddleware() {
        this.authService = new AuthService();
    }

    public AuthMiddleware(AuthService authService) {
        this.authService = authService;
    }

    public Handler requireAuth = ctx -> {
        String authHeader = ctx.header("Authorization");
        String token = JWTUtil.extractTokenFromHeader(authHeader);

        if (token == null) {
            ctx.status(401).json(Map.of(
                    "success", false,
                    "message", "Token de autorización requerido"
            ));
            return;
        }

        if (!authService.isTokenValid(token)) {
            ctx.status(401).json(Map.of(
                    "success", false,
                    "message", "Token inválido o expirado"
            ));
            return;
        }

        // Agregar información del usuario al contexto
        String username = authService.extractUsernameFromToken(token);
        ctx.attribute("username", username);
        ctx.attribute("token", token);
    };

    public Handler corsHandler = ctx -> {
        ctx.header("Access-Control-Allow-Origin", "*");
        ctx.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        ctx.header("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if (ctx.method().equals("OPTIONS")) {
            ctx.status(200);
        }
    };
}