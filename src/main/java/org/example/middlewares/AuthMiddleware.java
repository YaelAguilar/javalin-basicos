package org.example.middlewares;

import io.javalin.http.Handler;
import org.example.exceptions.UnauthorizedException;
import org.example.services.AuthService;
import org.example.utils.JWTUtil;

public class AuthMiddleware {
    private final AuthService authService;

    public AuthMiddleware(AuthService authService) {
        this.authService = authService;
    }

    public Handler requireAuth() {
        return ctx -> {
            String authHeader = ctx.header("Authorization");
            String token = JWTUtil.extractTokenFromHeader(authHeader);

            if (token == null) {
                throw new UnauthorizedException("Token de autorización requerido. El formato debe ser 'Bearer <token>'.");
            }

            if (!authService.isTokenValid(token)) {
                throw new UnauthorizedException("El token proporcionado es inválido o ha expirado.");
            }

            String username = authService.extractUsernameFromToken(token);
            if (username == null) {
                throw new UnauthorizedException("No se pudo extraer la identidad del usuario desde el token.");
            }

            ctx.attribute("username", username);
            ctx.attribute("token", token);
        };
    }
}