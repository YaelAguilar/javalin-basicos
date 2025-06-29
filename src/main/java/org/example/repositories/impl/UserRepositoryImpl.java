package org.example.repositories.impl;

import org.example.daos.UserDAO;
import org.example.models.User;
import org.example.repositories.UserRepository;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private final UserDAO userDAO;

    public UserRepositoryImpl() {
        this.userDAO = new UserDAO();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userDAO.findByUsername(username);
    }
}