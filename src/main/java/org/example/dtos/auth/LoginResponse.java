package org.example.dtos.auth;

import org.example.dtos.user.UserResponse;

public record LoginResponse(String token, UserResponse user) {
}