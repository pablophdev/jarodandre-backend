package cl.jajoyeria.auth.repository;

import cl.jajoyeria.auth.entity.Comuna;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComunaRepository extends JpaRepository<Comuna, Long> {
    List<Comuna> findByRegionIdOrderByNombre(String regionId);
}
