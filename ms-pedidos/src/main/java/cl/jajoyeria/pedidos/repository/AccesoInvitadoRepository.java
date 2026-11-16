package cl.jajoyeria.pedidos.repository;

import cl.jajoyeria.pedidos.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface AccesoInvitadoRepository extends JpaRepository<AccesoInvitado, UUID> {
    Optional<AccesoInvitado> findByPedidoId(Long pedidoId);
}
