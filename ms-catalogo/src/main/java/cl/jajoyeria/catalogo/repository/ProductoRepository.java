package cl.jajoyeria.catalogo.repository;

import cl.jajoyeria.catalogo.entity.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {
    long countByArchivadoEnIsNull();
    long countByArchivadoEnIsNullAndDestacadoTrue();
    Optional<Producto> findByIdAndArchivadoEnIsNull(Long id);
    List<Producto> findAllByIdInAndArchivadoEnIsNull(Collection<Long> ids);
}
