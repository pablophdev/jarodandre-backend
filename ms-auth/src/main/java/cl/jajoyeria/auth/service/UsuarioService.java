package cl.jajoyeria.auth.service;

import cl.jajoyeria.auth.dto.*;
import cl.jajoyeria.auth.entity.*;
import cl.jajoyeria.auth.repository.*;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final ComunaRepository comunas;

    @Transactional
    public UsuarioResponse registrar(UUID id, String correo, RegistroRequest datos) {
        if (usuarios.existsById(id)) throw new ResponseStatusException(CONFLICT, "El perfil ya existe");
        Usuario nuevo = new Usuario(id, correo, Rut.normalizar(datos.rut()), datos.nombre(),
                datos.telefono(), comuna(datos.comunaId()));
        return respuesta(usuarios.saveAndFlush(nuevo));
    }

    public UsuarioResponse perfil(UUID id) {
        return respuesta(activo(id));
    }

    @Transactional
    public UsuarioResponse editar(UUID id, PerfilRequest datos) {
        Usuario usuario = activo(id);
        usuario.editar(datos.nombre(), datos.telefono(), comuna(datos.comunaId()));
        return respuesta(usuario);
    }

    public Page<UsuarioResponse> listar(UUID actor, Pageable pagina) {
        exigirAdmin(actor);
        return usuarios.findAll(pagina).map(this::respuesta);
    }

    @Transactional
    public UsuarioResponse acceso(UUID actor, UUID id, AccesoRequest acceso) {
        exigirAdmin(actor);
        if (actor.equals(id) && (acceso.rol() != Rol.ADMIN || acceso.estado() != Estado.ACTIVO)) {
            throw new ResponseStatusException(CONFLICT, "No puedes quitar tu propio acceso administrativo");
        }
        Usuario usuario = buscar(id);
        usuario.cambiarAcceso(acceso.rol(), acceso.estado());
        return respuesta(usuario);
    }

    private void exigirAdmin(UUID actor) {
        if (activo(actor).getRol() != Rol.ADMIN) throw new ResponseStatusException(FORBIDDEN);
    }

    private Usuario activo(UUID id) {
        Usuario usuario = buscar(id);
        if (usuario.getEstado() != Estado.ACTIVO) throw new ResponseStatusException(FORBIDDEN, "Cuenta sin acceso");
        return usuario;
    }

    private Usuario buscar(UUID id) {
        return usuarios.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Perfil inexistente"));
    }

    private Comuna comuna(Long id) {
        return comunas.findById(id).orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Comuna inválida"));
    }

    private UsuarioResponse respuesta(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getCorreo(), u.getRut(), u.getNombre(), u.getTelefono(),
                u.getComuna().getId(), u.getComuna().getRegion().getId(), u.getRol(), u.getEstado());
    }
}
