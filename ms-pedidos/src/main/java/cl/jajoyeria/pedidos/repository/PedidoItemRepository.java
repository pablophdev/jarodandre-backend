package cl.jajoyeria.pedidos.repository;

import cl.jajoyeria.pedidos.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {
    @Query("select count(i) from PedidoItem i, Pedido p where i.pedidoId=p.id and p.usuarioId=:usuarioId and p.estadoPago=cl.jajoyeria.pedidos.entity.EstadoPago.PAGADO and i.productoId=:productoId")
    long comprasPagadas(UUID usuarioId, Long productoId);
    List<PedidoItem> findByPedidoIdOrderById(Long pedidoId);
}
