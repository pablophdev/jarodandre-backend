package cl.jajoyeria.pedidos.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(catalog = "ventas", name = "intentos_pago")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IntentoPago {
    @Id
    private UUID id;
    private Long pedidoId;
    private String proveedor;
    private String referenciaExterna;
    private Long montoClp;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Setter
    private EstadoPago estado;
    private Instant creadoEn;
    @Setter
    private Instant resueltoEn;
}
