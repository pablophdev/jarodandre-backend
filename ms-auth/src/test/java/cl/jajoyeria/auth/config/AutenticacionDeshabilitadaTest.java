package cl.jajoyeria.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.BadJwtException;
import cl.jajoyeria.auth.service.CorreoVerificado;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;

class AutenticacionDeshabilitadaTest {
    @Test
    void noAceptaTokensSinProveedorConfigurado() {
        assertThrows(BadJwtException.class, () -> new SecurityConfig().decoder().decode("token"));
    }

    @Test
    void noInventaCorreosVerificados() {
        var error = assertThrows(ResponseStatusException.class, () -> new CorreoVerificado().obtener(null));
        assertEquals(503, error.getStatusCode().value());
    }
}
