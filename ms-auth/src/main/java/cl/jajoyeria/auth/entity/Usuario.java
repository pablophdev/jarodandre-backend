package cl.jajoyeria.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(schema = "identidad", name = "usuarios")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Usuario {
    @Id
    private UUID id;
    private String correo;
    private String rut;
    private String nombre;
    private String telefono;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comuna_id")
    private Comuna comuna;
    @Enumerated(EnumType.STRING)
    private Rol rol;
    @Enumerated(EnumType.STRING)
    private Estado estado;
    private Instant creadoEn;
    private Instant actualizadoEn;
    @Version
    private Long version;

    public Usuario(UUID id, String correo, String rut, String nombre, String telefono, Comuna comuna) {
        this.id = id;
        this.correo = correo;
        this.rut = rut;
        this.rol = Rol.CLIENTE;
        this.estado = Estado.ACTIVO;
        this.creadoEn = Instant.now();
        editar(nombre, telefono, comuna);
    }

    public void editar(String nombre, String telefono, Comuna comuna) {
        this.nombre = nombre.strip();
        this.telefono = telefono;
        this.comuna = comuna;
        this.actualizadoEn = Instant.now();
    }

    public void cambiarAcceso(Rol rol, Estado estado) {
        this.rol = rol;
        this.estado = estado;
        this.actualizadoEn = Instant.now();
    }
}
