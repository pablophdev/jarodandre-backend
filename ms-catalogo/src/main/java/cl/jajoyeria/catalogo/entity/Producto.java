package cl.jajoyeria.catalogo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(catalog = "catalogo", name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    private String nombre;
    @Column(columnDefinition = "text")
    private String descripcion;
    private Long precioClp;
    private String imagenKey;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private String[] etiquetas;
    private String grupo;
    private String insignia;
    private boolean destacado;
    private int valoracion;
    private int cantidadResenas;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private Disponibilidad disponibilidad;
    private Instant archivadoEn;
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
