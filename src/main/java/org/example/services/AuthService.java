package org.example.services;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.example.dtos.auth.LoginRequest;
import org.example.dtos.auth.LoginResponse;
import org.example.exceptions.BadRequestException;
import org.example.exceptions.UnauthorizedException;
import org.example.mappers.UserMapper;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.example.repositories.impl.UserRepositoryImpl;
import org.example.utils.JWTUtil;
import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthService() {
        this.userRepository = new UserRepositoryImpl();
        this.userMapper = new UserMapper();
    }

    /**
     * Procesa una solicitud de inicio de sesión.
     *
     * @param loginRequest DTO con las credenciales del usuario.
     * @return un DTO LoginResponse con el token y la información del usuario si el login es exitoso.
     * @throws BadRequestException si el username o password están vacíos.
     * @throws UnauthorizedException si las credenciales son inválidas.
     */
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
                    return new LoginResponse(token, userMapper.toUserResponse(user));
                })
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));
    }

    /**
     * Invalida un token JWT añadiéndolo a la lista negra.
     *
     * @param token El token a invalidar.
     */
    public void logout(String token) {
        // El método isTokenValid ya maneja internamente los tokens nulos o vacíos.
        if (JWTUtil.isTokenValid(token)) {
            JWTUtil.blacklistToken(token);
        }
    }

    /**
     * Delega la validación del token a la utilidad JWT.
     *
     * @param token El token a verificar.
     * @return true si el token es válido, false en caso contrario.
     */
    public boolean isTokenValid(String token) {
        return JWTUtil.isTokenValid(token);
    }

    /**
     * Extrae el nombre de usuario de un token válido.
     *
     * @param token El token del cual extraer el nombre de usuario.
     * @return El nombre de usuario como String, o null si el token es inválido.
     */
    public String extractUsernameFromToken(String token) {
        if (isTokenValid(token)) {
            return JWTUtil.extractUsername(token);
        }
        return null;
    }

    /**
     * Compara una contraseña en texto plano con un hash de BCrypt.
     *
     * @param rawPassword La contraseña ingresada por el usuario.
     * @param hashedPassword El hash almacenado en la base de datos.
     * @return true si las contraseñas coinciden, false en caso contrario.
     */
    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        if (rawPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.verifyer().verify(rawPassword.toCharArray(), hashedPassword).verified;
        } catch (Exception e) {
            System.err.println("Error durante la verificación de la contraseña: " + e.getMessage());
            return false;
        }
    }
}