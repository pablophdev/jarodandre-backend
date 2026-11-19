package cl.jajoyeria.auth.controller;

import cl.jajoyeria.auth.dto.LoginRequest;
import cl.jajoyeria.auth.dto.RegistroAuthRequest;
import cl.jajoyeria.auth.dto.TokenResponse;
import cl.jajoyeria.auth.service.AuthService;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, inicio de sesión y clave pública")
public class AuthController {

    private final AuthService auth;
    private final RSAKey rsaKey;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar cliente",
            description = "Crea una cuenta con rol CLIENTE y devuelve un token de acceso."
    )
    public TokenResponse registrar(@Valid @RequestBody RegistroAuthRequest datos) {
        return auth.registrar(datos);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Verifica correo y contraseña de una cuenta activa."
    )
    public TokenResponse login(@Valid @RequestBody LoginRequest datos) {
        return auth.login(datos);
    }

    @GetMapping("/jwks")
    @Operation(
            summary = "Consultar clave pública",
            description = "Publica la clave pública para validar los JWT emitidos por ms-auth."
    )
    public Map<String, Object> clavesPublicas() {
        return new JWKSet(rsaKey.toPublicJWK()).toJSONObject();
    }
}