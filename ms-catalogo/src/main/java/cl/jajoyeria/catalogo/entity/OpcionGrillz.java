package cl.jajoyeria.catalogo.entity;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(catalog="catalogo",name="opciones_grillz") @Getter @NoArgsConstructor
public class OpcionGrillz {
    @Id private String codigo;
    private String tipo;
    private String nombre;
    private String color;
    private long precioClp;
}
