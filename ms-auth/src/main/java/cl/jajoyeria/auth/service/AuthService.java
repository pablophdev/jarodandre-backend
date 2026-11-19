package cl.jajoyeria.auth.service;

import cl.jajoyeria.auth.dto.*;
import cl.jajoyeria.auth.entity.Estado;
import cl.jajoyeria.auth.entity.Usuario;
import cl.jajoyeria.auth.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final UsuarioRepository usuarios;
    private final UsuarioService perfiles;
    private final PasswordEncoder passwords;
    private final TokenService tokens;
    private final String hashFicticio;

    public AuthService(
            UsuarioRepository usuarios,
            UsuarioService perfiles,
            PasswordEncoder passwords,
            TokenService tokens
    ) {
        this.usuarios = usuarios;
        this.perfiles = perfiles;
        this.passwords = passwords;
        this.tokens = tokens;
        this.hashFicticio = passwords.encode(UUID.randomUUID().toString());
    }

    @Transactional
    public TokenResponse registrar(RegistroAuthRequest datos) {
        if (datos.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(
                    BAD_REQUEST,
                    "La contraseña supera el límite de 72 bytes"
            );
        }

        UUID id = UUID.randomUUID();

        RegistroRequest perfil = new RegistroRequest(
                datos.rut(),
                datos.nombre(),
                datos.telefono(),
                datos.comunaId()
        );

        UsuarioResponse respuesta = perfiles.registrar(id, datos.correo(), perfil);

        Usuario usuario = usuarios.findById(id).orElseThrow();
        usuario.establecerPasswordHash(passwords.encode(datos.password()));
        usuarios.flush();

        return tokens.emitir(respuesta);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest datos) {
        if (datos.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw credencialesInvalidas();
        }

        var encontrado = usuarios.findByCorreo(datos.correo());

        String hash = encontrado
                .map(Usuario::getPasswordHash)
                .orElse(hashFicticio);

        boolean coincide = passwords.matches(datos.password(), hash);

        if (encontrado.isEmpty()
                || !coincide
                || encontrado.get().getPasswordHash() == null
                || encontrado.get().getEstado() != Estado.ACTIVO) {
            throw credencialesInvalidas();
        }

        UsuarioResponse perfil = perfiles.perfil(encontrado.get().getId());
        return tokens.emitir(perfil);
    }

    private ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(
                UNAUTHORIZED,
                "Correo o contraseña incorrectos, o cuenta sin acceso"
        );
    }
}