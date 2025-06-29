package org.example.mappers;

import org.example.dtos.user.UserResponse;
import org.example.models.User;

public class UserMapper {

    /**
     * @param user La entidad de dominio a convertir.
     * @return Un DTO con los datos seguros para el cliente.
     */
    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(user.getId(), user.getUsername());
    }
}