package cl.jajoyeria.catalogo.repository;

import cl.jajoyeria.catalogo.entity.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    List<Categoria> findAllByOrderByNombre();
}
