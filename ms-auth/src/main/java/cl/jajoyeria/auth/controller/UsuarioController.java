package cl.jajoyeria.auth.controller;

import cl.jajoyeria.auth.dto.*;
import cl.jajoyeria.auth.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
@Tag(name = "Perfiles", description = "Consulta y actualización del perfil de la identidad autenticada")
public class UsuarioController {
    private final UsuarioService usuarios;
    private final CorreoVerificado correo;
    private final AuthService auth;

    @PostMapping("/admin/usuarios")
    @Operation(summary = "Crear cuenta desde administración")
    @SecurityRequirement(name = "BearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse crearCuenta(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CrearUsuarioRequest datos) {
        return auth.crearUsuario(UUID.fromString(jwt.getSubject()), datos);
    }

    @Operation(summary = "Crear perfil", description = "Perfil de una identidad existente. Para crear una cuenta con contraseña usar /v1/auth/registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Perfil creado"),
            @ApiResponse(responseCode = "400", description = "RUT, comuna o datos del perfil inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "409", description = "El perfil ya existe")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse registrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RegistroRequest datos) {
        return usuarios.registrar(UUID.fromString(jwt.getSubject()), correo.obtener(jwt), datos);
    }

    @Operation(summary = "Consultar perfil", description = "Devuelve el perfil activo de la identidad autenticada. La sesión usa el JWT emitido por ms-auth.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "La cuenta no tiene acceso activo"),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/me")
    public UsuarioResponse perfil(@AuthenticationPrincipal Jwt jwt) {
        return usuarios.perfil(UUID.fromString(jwt.getSubject()));
    }

    @Operation(summary = "Actualizar perfil", description = "Actualiza nombre, teléfono y comuna del perfil activo. El teléfono es opcional.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil actualizado"),
            @ApiResponse(responseCode = "400", description = "Comuna o datos del perfil inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "La cuenta no tiene acceso activo"),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/me")
    public UsuarioResponse editar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody PerfilRequest datos) {
        return usuarios.editar(UUID.fromString(jwt.getSubject()), datos);
    }

    @Operation(summary = "Listar usuarios", description = "Obtiene una página de perfiles. Requiere que el perfil autenticado tenga rol ADMIN y estado ACTIVO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de usuarios obtenida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN activo)")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Usuarios", description = "Gestión administrativa de perfiles y acceso")
    @GetMapping("/admin/usuarios")
    public Page<UsuarioResponse> listar(@AuthenticationPrincipal Jwt jwt, @ParameterObject Pageable pagina) {
        return usuarios.listar(UUID.fromString(jwt.getSubject()), pagina);
    }

    @Operation(summary = "Cambiar acceso de usuario", description = "Actualiza el rol (CLIENTE, EMPLEADO o ADMIN) y estado (PENDIENTE, ACTIVO o BLOQUEADO) de un perfil. Requiere rol ADMIN y estado ACTIVO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Acceso actualizado"),
            @ApiResponse(responseCode = "400", description = "Rol o estado inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN activo)"),
            @ApiResponse(responseCode = "404", description = "Perfil no encontrado"),
            @ApiResponse(responseCode = "409", description = "No se puede quitar el propio acceso administrativo o el registro tuvo un conflicto")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Usuarios")
    @PatchMapping("/admin/usuarios/{id}/acceso")
    public UsuarioResponse acceso(@AuthenticationPrincipal Jwt jwt,
                                 @Parameter(description = "Identificador UUID del usuario", example = "015d5f49-c538-4d59-b463-ac5c0a447c99") @PathVariable UUID id,
                                 @Valid @RequestBody AccesoRequest datos) {
        return usuarios.acceso(UUID.fromString(jwt.getSubject()), id, datos);
    }
}
