package cl.jajoyeria.comunicaciones.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(schema = "comunicaciones", name = "suscripciones")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Suscripcion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String correo;
    private Instant altaEn;
    private Instant bajaEn;
}
