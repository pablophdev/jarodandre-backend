package cl.jajoyeria.pedidos.dto;

import java.util.UUID;

public record CarritoSesion(UUID carritoId, String tokenInvitado) {}
