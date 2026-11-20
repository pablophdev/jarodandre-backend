package cl.jajoyeria.catalogo.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(catalog="catalogo",name="disenos_grillz") @Getter @NoArgsConstructor
public class DisenoGrillz {
    @Id private UUID id;
    private UUID usuarioId;
    private String autor;
    private String nombre;
    @Column(columnDefinition="text") private String configuracion;
    private long estimadoClp;
    private String estado;
    private Instant creadoEn;
    public DisenoGrillz(UUID usuario, String autor, String nombre, String configuracion, long precio) {
        id=UUID.randomUUID();usuarioId=usuario;this.autor=autor;this.nombre=nombre;
        this.configuracion=configuracion;estimadoClp=precio;estado="BORRADOR";creadoEn=Instant.now();
    }
    public void solicitar(){estado="SOLICITADO";}
}
