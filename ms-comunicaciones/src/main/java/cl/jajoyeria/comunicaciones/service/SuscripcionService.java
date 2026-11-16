package cl.jajoyeria.comunicaciones.service;

import cl.jajoyeria.comunicaciones.dto.*;
import cl.jajoyeria.comunicaciones.repository.SuscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
import static cl.jajoyeria.comunicaciones.dto.AltaRespuesta.Estado.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SuscripcionService {
    private final SuscripcionRepository suscripciones;

    @Transactional
    public AltaRespuesta alta(String correo) {
        if (suscripciones.insertar(correo) == 1) return new AltaRespuesta(ALTA);
        if (suscripciones.reactivar(correo) == 1) return new AltaRespuesta(REACTIVADA);
        return new AltaRespuesta(YA_ACTIVA);
    }

    public Page<SuscriptorVista> activas(String q, int pagina, int tamano) {
        return suscripciones.activas(q.strip().toLowerCase(Locale.ROOT), PageRequest.of(pagina, tamano))
                .map(s -> new SuscriptorVista(s.getId(), s.getCorreo(), s.getAltaEn()));
    }

    @Transactional
    public void baja(long id) {
        if (suscripciones.darDeBaja(id) == 0 && !suscripciones.existsById(id)) {
            throw new ResponseStatusException(NOT_FOUND, "Suscripción inexistente");
        }
    }
}
