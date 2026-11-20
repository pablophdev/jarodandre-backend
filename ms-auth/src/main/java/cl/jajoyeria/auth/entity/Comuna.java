package cl.jajoyeria.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Table(catalog = "identidad", name = "comunas")
@Getter
public class Comuna {
    @Id
    private Long id;
    private String nombre;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_id")
    private Region region;
}
