package cl.jajoyeria.pedidos.repository;

import cl.jajoyeria.pedidos.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;

public interface CarritoRepository extends JpaRepository<Carrito, UUID> {
    Optional<Carrito> findByUsuarioId(UUID usuarioId);
    Optional<Carrito> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Carrito c where c.id = :id")
    Optional<Carrito> bloquear(UUID id);
}
