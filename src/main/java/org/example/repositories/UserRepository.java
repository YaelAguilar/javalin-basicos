package org.example.repositories;

import org.example.models.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
}