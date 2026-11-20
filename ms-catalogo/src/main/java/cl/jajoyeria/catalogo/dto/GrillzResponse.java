package cl.jajoyeria.catalogo.dto;
import java.time.Instant;
import java.util.UUID;
public record GrillzResponse(UUID id, UUID usuarioId, String autor, String nombre, GrillzRequest configuracion,
        long estimadoClp, String estado, Instant creadoEn) {}
