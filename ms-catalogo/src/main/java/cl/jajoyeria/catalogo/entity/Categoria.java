package cl.jajoyeria.catalogo.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(schema = "catalogo", name = "categorias")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String slug;
}
