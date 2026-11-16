package cl.jajoyeria.auth.repository;

import cl.jajoyeria.auth.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RegionRepository extends JpaRepository<Region, String> {
    List<Region> findAllByOrderByNombre();
}
