package cl.jajoyeria.comunicaciones.dto;

public record AltaRespuesta(Estado estado) {
    public enum Estado { ALTA, YA_ACTIVA, REACTIVADA }
}
