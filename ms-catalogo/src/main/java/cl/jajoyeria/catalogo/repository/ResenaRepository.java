package cl.jajoyeria.catalogo.repository;

import cl.jajoyeria.catalogo.entity.Resena;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import java.util.*;

public interface ResenaRepository extends JpaRepository<Resena,Long> {
    Optional<Resena> findByProductoIdAndUsuarioId(Long productoId, UUID usuarioId);
    Page<Resena> findByProductoIdOrderByCreadoEnDesc(Long productoId, Pageable pagina);
    long countByProductoId(Long productoId);
    @Query("select avg(r.estrellas) from Resena r where r.productoId=:productoId")
    Double promedio(Long productoId);
}
