package cl.jajoyeria.catalogo.repository;

import cl.jajoyeria.catalogo.entity.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface PublicacionRepository extends JpaRepository<Publicacion, Long> {
    Optional<Publicacion> findBySlugAndEstado(String slug, EstadoPublicacion estado);
    List<Publicacion> findByEstadoOrderByPublicadoEnDesc(EstadoPublicacion estado);
}
