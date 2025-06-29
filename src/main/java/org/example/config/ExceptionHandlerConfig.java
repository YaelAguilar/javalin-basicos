package org.example.config;

import io.javalin.Javalin;
import org.example.exceptions.ApiBaseException;
import java.util.Map;

public class ExceptionHandlerConfig {

    public static void register(Javalin app) {
        app.exception(ApiBaseException.class, (e, ctx) ->
                ctx.status(e.getStatusCode()).json(Map.of("success", false, "message", e.getMessage()))
        );

        app.exception(io.javalin.http.BadRequestResponse.class, (e, ctx) ->
                ctx.status(400).json(Map.of("success", false, "message", "Petición incorrecta: " + e.getMessage()))
        );

        app.exception(IllegalStateException.class, (e, ctx) -> {
            System.err.println("Error de estado interno: " + e.getMessage());
            ctx.status(500).json(Map.of("success", false, "message", "Error interno del servidor: " + e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) -> {
            System.err.println("Error no controlado (" + e.getClass().getName() + "): " + e.getMessage());
            ctx.status(500).json(Map.of("success", false, "message", "Error interno del servidor."));
        });

        app.error(404, ctx -> {
            if (ctx.result() == null) {
                ctx.json(Map.of("success", false, "message", "Endpoint no encontrado: " + ctx.method() + " " + ctx.path()));
            }
        });
    }
}