package cl.jajoyeria.auth.controller;

import cl.jajoyeria.auth.dto.*;
import cl.jajoyeria.auth.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarios;
    private final CorreoVerificado correo;

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RegistroRequest datos) {
        return usuarios.registrar(UUID.fromString(jwt.getSubject()), correo.obtener(jwt), datos);
    }

    @GetMapping("/me")
    public UsuarioResponse perfil(@AuthenticationPrincipal Jwt jwt) {
        return usuarios.perfil(UUID.fromString(jwt.getSubject()));
    }

    @PutMapping("/me")
    public UsuarioResponse editar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilRequest datos) {
        return usuarios.editar(UUID.fromString(jwt.getSubject()), datos);
    }

    @GetMapping("/admin/usuarios")
    public Page<UsuarioResponse> listar(@AuthenticationPrincipal Jwt jwt, Pageable pagina) {
        return usuarios.listar(UUID.fromString(jwt.getSubject()), pagina);
    }

    @PatchMapping("/admin/usuarios/{id}/acceso")
    public UsuarioResponse acceso(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                 @Valid @RequestBody AccesoRequest datos) {
        return usuarios.acceso(UUID.fromString(jwt.getSubject()), id, datos);
    }
}
