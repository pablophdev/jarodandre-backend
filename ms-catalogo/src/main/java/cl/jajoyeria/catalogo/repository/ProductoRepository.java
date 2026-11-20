package cl.jajoyeria.catalogo.repository;

import cl.jajoyeria.catalogo.entity.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface ProductoRepository extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id=:id and p.archivadoEn is null")
    Optional<Producto> bloquear(long id);
    long countByArchivadoEnIsNull();
    long countByArchivadoEnIsNullAndDestacadoTrue();
    Optional<Producto> findByIdAndArchivadoEnIsNull(Long id);
    List<Producto> findAllByIdInAndArchivadoEnIsNull(Collection<Long> ids);
}
