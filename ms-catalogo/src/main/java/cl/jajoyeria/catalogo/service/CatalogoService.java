package cl.jajoyeria.catalogo.service;

import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.entity.*;
import cl.jajoyeria.catalogo.repository.*;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.text.Normalizer;
import java.util.*;
import static org.springframework.http.HttpStatus.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoService {
    private final CategoriaRepository categorias;
    private final ProductoRepository productos;

    private CategoriaResponse categoria(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getSlug());
    }

    public List<CategoriaResponse> categorias() {
        return categorias.findAllByOrderByNombre().stream().map(this::categoria).toList();
    }

    private ProductoResponse respuesta(Producto p) {
        return new ProductoResponse(p.getId(), categoria(p.getCategoria()), p.getNombre(), p.getDescripcion(),
                p.getPrecioClp(), p.getImagenKey(), List.of(p.getEtiquetas()), p.getGrupo(), p.getInsignia(),
                p.isDestacado(), p.getValoracion(), p.getCantidadResenas(), p.getDisponibilidad(), p.getArchivadoEn());
    }

    public Page<ProductoResponse> buscar(String q, String categoria, String grupo, String insignia, Boolean destacado,
                                         Long minimo, Long maximo, String orden, int pagina, int tamano) {
        Sort sort = switch (orden) {
            case "precio-asc" -> Sort.by("precioClp");
            case "precio-desc" -> Sort.by("precioClp").descending();
            case "nombre-asc" -> Sort.by("nombre");
            case "nombre-desc" -> Sort.by("nombre").descending();
            case "id" -> Sort.by("id");
            default -> throw new ResponseStatusException(BAD_REQUEST, "Orden desconocido");
        };
        if (minimo != null && maximo != null && minimo > maximo) throw new ResponseStatusException(BAD_REQUEST, "Rango inválido");
        return productos.findAll((root, query, cb) -> {
            List<Predicate> filtros = new ArrayList<>();
            filtros.add(cb.isNull(root.get("archivadoEn")));
            if (categoria != null) filtros.add(cb.equal(root.get("categoria").get("slug"), categoria));
            if (grupo != null) filtros.add(cb.equal(root.get("grupo"), grupo));
            if (insignia != null) filtros.add(cb.equal(root.get("insignia"), insignia));
            if (destacado != null) filtros.add(cb.equal(root.get("destacado"), destacado));
            if (minimo != null) filtros.add(cb.ge(root.get("precioClp"), minimo));
            if (maximo != null) filtros.add(cb.le(root.get("precioClp"), maximo));
            if (q != null && !q.isBlank()) {
                Expression<String> nombre = cb.lower(root.get("nombre"));
                String letras = "áéíóúüñ";
                for (int i = 0; i < letras.length(); i++) {
                    nombre = cb.function("replace", String.class, nombre, cb.literal(letras.substring(i,i+1)),
                            cb.literal("aeiouun".substring(i,i+1)));
                }
                String limpio = Normalizer.normalize(q.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "");
                for (String palabra : limpio.split("\\s+")) {
                    filtros.add(cb.gt(cb.locate(nombre, palabra), 0));
                }
            }
            return cb.and(filtros.toArray(Predicate[]::new));
        }, PageRequest.of(pagina, tamano, sort.and(Sort.by("id")))).map(this::respuesta);
    }

    public ResumenCatalogo resumen() {
        return new ResumenCatalogo(productos.countByArchivadoEnIsNull(), productos.countByArchivadoEnIsNullAndDestacadoTrue());
    }

    private Producto visible(long id) {
        return productos.findByIdAndArchivadoEnIsNull(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Producto inexistente"));
    }

    public ProductoResponse obtener(long id) {
        return respuesta(visible(id));
    }

    @Transactional
    public ProductoResponse guardar(Long id, ProductoRequest r) {
        Producto p = id == null ? new Producto() : productos.bloquear(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Producto no encontrado"));
        p.setCategoria(categorias.findById(r.categoriaId()).orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Categoría inválida")));
        p.setNombre(r.nombre().strip());
        p.setDescripcion(r.descripcion().strip());
        p.setPrecioClp(r.precioClp());
        p.setImagenKey(r.imagenKey());
        p.setEtiquetas(r.etiquetas().toArray(String[]::new));
        p.setGrupo(r.grupo());
        p.setInsignia(r.insignia());
        p.setDestacado(r.destacado());
        p.setDisponibilidad(r.disponibilidad());
        return respuesta(productos.saveAndFlush(p));
    }

    @Transactional
    public void archivar(long id) {
        visible(id).setArchivadoEn(Instant.now());
    }

    public List<PrecioResponse> cotizar(List<Long> ids) {
        Set<Long> unicos = new LinkedHashSet<>(ids);
        var encontrados = productos.findAllByIdInAndArchivadoEnIsNull(unicos);
        if (encontrados.size() != unicos.size()) throw new ResponseStatusException(CONFLICT, "Uno o más productos ya no están disponibles");
        return encontrados.stream().map(p -> new PrecioResponse(p.getId(), p.getNombre(), p.getPrecioClp(), p.getDisponibilidad())).toList();
    }
}
