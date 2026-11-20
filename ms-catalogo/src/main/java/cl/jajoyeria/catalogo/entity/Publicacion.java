package cl.jajoyeria.catalogo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(catalog = "catalogo", name = "publicaciones")
public class Publicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    private String slug;
    private String titulo;
    private String autorVisible;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String[] parrafos;
    private String portadaKey;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
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
