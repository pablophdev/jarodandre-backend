package cl.jajoyeria.catalogo.dto;

import java.time.Instant;
import java.util.List;
import cl.jajoyeria.catalogo.entity.Disponibilidad;

public record ProductoResponse(
        Long id, CategoriaResponse categoria, String nombre, String descripcion, Long precioClp,
        String imagenKey, List<String> etiquetas, String grupo, String insignia, boolean destacado,
        int valoracion, int cantidadResenas, Disponibilidad disponibilidad, Instant archivadoEn) {}
