package cl.jajoyeria.comunicaciones.service;

import cl.jajoyeria.comunicaciones.dto.*;
import cl.jajoyeria.comunicaciones.entity.MensajeContacto;
import cl.jajoyeria.comunicaciones.repository.MensajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.Locale;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MensajeService {
    private final MensajeRepository mensajes;

    @Transactional
    public void recibir(MensajeRequest datos) {
        mensajes.save(new MensajeContacto(datos.nombre(), datos.correo(), datos.mensaje()));
    }

    public Page<MensajeVista> bandeja(String q, String lectura, int pagina, int tamano) {
        return mensajes.buscar(q.strip().toLowerCase(Locale.ROOT), lectura, PageRequest.of(pagina, tamano)).map(this::vista);
    }

    public ResumenMensajes resumen() {
        return new ResumenMensajes(mensajes.count(), mensajes.countByLeidoEnIsNull());
    }

    @Transactional
    public MensajeVista marcar(long id, boolean leido) {
        var mensaje = existente(id);
        mensaje.marcarLectura(leido);
        return vista(mensaje);
    }

    @Transactional
    public void eliminar(long id) {
        mensajes.delete(existente(id));
    }

    private MensajeContacto existente(long id) {
        return mensajes.bloquear(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Mensaje inexistente"));
    }

    private MensajeVista vista(MensajeContacto m) {
        return new MensajeVista(m.getId(), m.getNombre(), m.getCorreo(), m.getMensaje(), m.getCreadoEn(), m.getLeidoEn());
    }
}
