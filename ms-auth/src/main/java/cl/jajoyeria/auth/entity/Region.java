package cl.jajoyeria.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(schema = "identidad", name = "regiones")
@Getter
public class Region {
    @Id
    private String id;
    private String nombre;

}
