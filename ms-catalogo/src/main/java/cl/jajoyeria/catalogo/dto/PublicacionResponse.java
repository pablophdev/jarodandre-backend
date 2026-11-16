package cl.jajoyeria.catalogo.dto;

import java.time.Instant;
import java.util.List;
import cl.jajoyeria.catalogo.entity.EstadoPublicacion;

public record PublicacionResponse(
        Long id, String slug, String titulo, String autorVisible, List<String> parrafos,
        String portadaKey, EstadoPublicacion estado, Instant publicadoEn) {}
