package cl.jajoyeria.catalogo.dto;

import java.time.Instant;

public record ResenaResponse(Long id, String autor, int estrellas, String comentario, Instant creadoEn) {}
