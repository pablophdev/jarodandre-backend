package cl.jajoyeria.catalogo.service;

import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.entity.*;
import cl.jajoyeria.catalogo.repository.PublicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import static org.springframework.http.HttpStatus.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PublicacionService {
    private final PublicacionRepository publicaciones;

    private PublicacionResponse respuesta(Publicacion p) {
        return new PublicacionResponse(p.getId(), p.getSlug(), p.getTitulo(), p.getAutorVisible(),
                List.of(p.getParrafos()), p.getPortadaKey(), p.getEstado(), p.getPublicadoEn());
    }

    public List<PublicacionResponse> listar(boolean administracion) {
        var lista = administracion ? publicaciones.findAll(Sort.by("id").descending())
                : publicaciones.findByEstadoOrderByPublicadoEnDesc(EstadoPublicacion.PUBLICADO);
        return lista.stream().map(this::respuesta).toList();
    }

    public PublicacionResponse porSlug(String slug) {
        return publicaciones.findBySlugAndEstado(slug, EstadoPublicacion.PUBLICADO)
                .map(this::respuesta).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @Transactional
    public PublicacionResponse guardar(Long id, PublicacionRequest r) {
        Publicacion p = id == null ? new Publicacion() :
                publicaciones.findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        if (r.estado() == EstadoPublicacion.PUBLICADO && r.parrafos().isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "La publicación requiere contenido");
        }
        p.setSlug(r.slug());
        p.setTitulo(r.titulo().strip());
        p.setAutorVisible(r.autorVisible().strip());
        p.setParrafos(r.parrafos().toArray(String[]::new));
        p.setPortadaKey(r.portadaKey());
        if (r.estado() == EstadoPublicacion.PUBLICADO && p.getEstado() != EstadoPublicacion.PUBLICADO) {
            p.setPublicadoEn(Instant.now());
        }
        if (r.estado() == EstadoPublicacion.BORRADOR) p.setPublicadoEn(null);
        p.setEstado(r.estado());
        return respuesta(publicaciones.saveAndFlush(p));
    }
}
