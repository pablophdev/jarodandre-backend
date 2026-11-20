package cl.jajoyeria.pedidos.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(catalog = "ventas", name = "accesos_invitado")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccesoInvitado {
    @Id
    private UUID id;
    private Long pedidoId;
    private String tokenHash;
    private Instant venceEn;
    @Setter
    private Instant revocadoEn;
}
