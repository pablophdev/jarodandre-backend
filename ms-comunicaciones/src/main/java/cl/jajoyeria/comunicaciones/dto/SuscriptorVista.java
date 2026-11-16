package cl.jajoyeria.comunicaciones.dto;

import java.time.Instant;

public record SuscriptorVista(Long id, String correo, Instant altaEn) {}
