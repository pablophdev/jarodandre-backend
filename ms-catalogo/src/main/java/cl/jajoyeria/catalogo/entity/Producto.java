package cl.jajoyeria.catalogo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(schema = "catalogo", name = "productos")
public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
    private String nombre;
    private String descripcion;
    private Long precioClp;
    private String imagenKey;
    private String[] etiquetas;
    private String grupo;
    private String insignia;
    private boolean destacado;
    private int valoracion;
    private int cantidadResenas;
    @Enumerated(EnumType.STRING)
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
