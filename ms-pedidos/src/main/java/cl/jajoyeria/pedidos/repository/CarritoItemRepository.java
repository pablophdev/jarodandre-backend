package cl.jajoyeria.pedidos.repository;

import cl.jajoyeria.pedidos.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
    List<CarritoItem> findByCarritoIdOrderById(UUID carritoId);
    Optional<CarritoItem> findByCarritoIdAndProductoId(UUID carritoId, Long productoId);
    long countByCarritoId(UUID carritoId);
    void deleteByCarritoId(UUID carritoId);
}
