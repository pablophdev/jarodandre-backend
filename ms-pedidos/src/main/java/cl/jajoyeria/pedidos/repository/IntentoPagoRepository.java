package cl.jajoyeria.pedidos.repository;

import cl.jajoyeria.pedidos.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface IntentoPagoRepository extends JpaRepository<IntentoPago, UUID> {
    Optional<IntentoPago> findByPedidoIdAndEstado(Long pedidoId, EstadoPago estado);
}
