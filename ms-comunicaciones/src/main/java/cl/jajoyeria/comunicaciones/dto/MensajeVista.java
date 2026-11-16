package cl.jajoyeria.comunicaciones.dto;

import java.time.Instant;

public record MensajeVista(Long id, String nombre, String correo, String mensaje, Instant creadoEn, Instant leidoEn) {}
