package cl.jajoyeria.pedidos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "ventas", name = "carritos")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Carrito {
    @Id
    private UUID id;
    private UUID usuarioId;
    private String tokenHash;
    private Instant creadoEn;
}
