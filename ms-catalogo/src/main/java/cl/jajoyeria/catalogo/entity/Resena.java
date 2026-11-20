package cl.jajoyeria.catalogo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(catalog="catalogo", name="resenas")
@Getter
@NoArgsConstructor(access=AccessLevel.PROTECTED)
public class Resena {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private Long productoId;
    private UUID usuarioId;
    private String autor;
    private int estrellas;
    private String comentario;
    private Instant creadoEn;
    private Instant actualizadoEn;

    public Resena(long productoId, UUID usuarioId) {
        this.productoId=productoId;
        this.usuarioId=usuarioId;
        this.creadoEn=Instant.now();
    }

    public void editar(String autor, int estrellas, String comentario) {
        this.autor=autor;
        this.estrellas=estrellas;
        this.comentario=comentario == null ? "" : comentario.strip();
        this.actualizadoEn=Instant.now();
    }
}
