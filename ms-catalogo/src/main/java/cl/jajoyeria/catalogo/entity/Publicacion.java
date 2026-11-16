package cl.jajoyeria.catalogo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(schema = "catalogo", name = "publicaciones")
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    private String slug;
    private String titulo;
    private String autorVisible;
    private String[] parrafos;
    private String portadaKey;
    @Enumerated(EnumType.STRING)
    private EstadoPublicacion estado;
    private Instant publicadoEn;
    private Instant creadoEn;
    private Instant actualizadoEn;
    @Version
    private Long version;

    @PrePersist
    private void crear() {
        creadoEn = Instant.now();
        actualizadoEn = creadoEn;
    }

    @PreUpdate
    private void actualizar() {
        actualizadoEn = Instant.now();
    }
}
