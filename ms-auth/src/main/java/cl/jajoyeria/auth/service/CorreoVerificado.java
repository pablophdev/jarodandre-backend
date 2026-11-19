package cl.jajoyeria.auth.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Punto de integración pendiente: nunca confía en correos aportados por el cliente. */
@Service
public class CorreoVerificado {
    public String obtener(Jwt token) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "No hay un proveedor de identidad configurado para verificar el correo");
    }
}
