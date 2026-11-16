package cl.jajoyeria.pedidos.repository;

import cl.jajoyeria.pedidos.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.*;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByCarritoIdAndClaveCheckout(UUID carritoId, UUID claveCheckout);
    Optional<Pedido> findByCarritoIdAndEstadoPago(UUID carritoId, EstadoPago estado);
    Page<Pedido> findByUsuarioId(UUID usuarioId, Pageable pagina);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pedido p where p.id = :id")
    Optional<Pedido> bloquear(Long id);
}
