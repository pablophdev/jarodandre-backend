package cl.jajoyeria.catalogo.repository;
import cl.jajoyeria.catalogo.entity.DisenoGrillz;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface DisenoGrillzRepository extends JpaRepository<DisenoGrillz,UUID> {
    Page<DisenoGrillz> findByUsuarioIdOrderByCreadoEnDesc(UUID usuario, Pageable pagina);
    Page<DisenoGrillz> findByEstadoOrderByCreadoEnDesc(String estado, Pageable pagina);
}
