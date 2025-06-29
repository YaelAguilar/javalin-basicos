package org.example.routes;

import io.javalin.Javalin;

@FunctionalInterface
public interface Router {
    void register(Javalin app);
}