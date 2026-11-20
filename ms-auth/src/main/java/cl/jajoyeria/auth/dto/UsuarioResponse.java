package cl.jajoyeria.auth.dto;

import java.util.UUID;
import java.time.Instant;
import cl.jajoyeria.auth.entity.*;

public record UsuarioResponse(
        UUID id, String correo, String rut, String nombre, String telefono,
        Long comunaId, String regionId, Rol rol, Estado estado, Instant creadoEn) {}
