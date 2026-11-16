package cl.jajoyeria.pedidos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ventas", name = "pedido_items")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PedidoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pedidoId;
    private Long productoId;
    private String nombreProducto;
    private Long precioUnitarioClp;
    private Integer cantidad;
    private Long subtotalClp;
}
