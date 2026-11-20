package cl.jajoyeria.pedidos.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(catalog = "ventas", name = "pedidos")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private UUID carritoId;
    @Setter
    private UUID usuarioId;
    private UUID claveCheckout;
    private String nombreEntrega;
    private String correoEntrega;
    private String direccionEntrega;
    private String ciudadEntrega;
    private Long totalClp;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Setter
    private EstadoPago estadoPago;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Setter
    private EstadoEntrega estadoEntrega;
    private Instant creadoEn;
    @Setter
    private Instant pagadoEn;
}
