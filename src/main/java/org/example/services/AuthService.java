package org.example.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.dtos.auth.LoginRequest;
import org.example.dtos.auth.LoginResponse;
import org.example.dtos.user.UserResponse;
import org.example.exceptions.BadRequestException;
import org.example.exceptions.UnauthorizedException;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.example.repositories.impl.UserRepositoryImpl;
import org.example.utils.JWTUtil;
import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService() {
        this.userRepository = new UserRepositoryImpl();
    }

    public LoginResponse login(LoginRequest loginRequest) {
        if (loginRequest.username() == null || loginRequest.username().trim().isEmpty() ||
                loginRequest.password() == null || loginRequest.password().trim().isEmpty()) {
            throw new BadRequestException("Usuario y contraseña son requeridos.");
        }

        Optional<User> userOpt = userRepository.findByUsername(loginRequest.username().trim());

        return userOpt
                .filter(user -> verifyPassword(loginRequest.password(), user.getPassword()))
                .map(user -> {
                    String token = JWTUtil.generateToken(user.getUsername());
                    UserResponse userResponse = new UserResponse(user.getId(), user.getUsername());
                    return new LoginResponse(token, userResponse);
                })
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));
    }

    public void logout(String token) {
        if (token != null && JWTUtil.isTokenValid(token)) {
            JWTUtil.blacklistToken(token);
        }
    }

    public boolean isTokenValid(String token) {
        return JWTUtil.isTokenValid(token);
    }

    public String extractUsernameFromToken(String token) {
        if (token != null && isTokenValid(token)) {
            return JWTUtil.extractUsername(token);
        }
        return null;
    }

    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        return BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword).verified;
    }
}