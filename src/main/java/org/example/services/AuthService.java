package org.example.services;

import org.example.daos.UserDAO;
import org.example.models.User;
import org.example.utils.JWTUtil;
import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public LoginResult login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return new LoginResult(false, null, null, "Usuario y contraseña son requeridos");
        }

        Optional<User> userOpt = userDAO.authenticate(username, password);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = JWTUtil.generateToken(user.getUsername());
            return new LoginResult(true, token, user, "Login exitoso");
        } else {
            return new LoginResult(false, null, null, "Credenciales inválidas");
        }
    }

    public LogoutResult logout(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new LogoutResult(false, "Token requerido");
        }

        if (JWTUtil.isTokenValid(token)) {
            JWTUtil.blacklistToken(token);
            return new LogoutResult(true, "Logout exitoso");
        } else {
            return new LogoutResult(false, "Token inválido");
        }
    }

    public boolean isTokenValid(String token) {
        return JWTUtil.isTokenValid(token);
    }

    public String extractUsernameFromToken(String token) {
        if (JWTUtil.isTokenValid(token)) {
            return JWTUtil.extractUsername(token);
        }
        return null;
    }

    public static class LoginResult {
        private final boolean success;
        private final String token;
        private final User user;
        private final String message;

        public LoginResult(boolean success, String token, User user, String message) {
            this.success = success;
            this.token = token;
            this.user = user;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getToken() { return token; }
        public User getUser() { return user; }
        public String getMessage() { return message; }
    }

    public static class LogoutResult {
        private final boolean success;
        private final String message;

        public LogoutResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}