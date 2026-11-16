package cl.jajoyeria.comunicaciones.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(schema = "comunicaciones", name = "mensajes_contacto")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MensajeContacto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String correo;
    private String mensaje;
    private Instant creadoEn;
    private Instant leidoEn;

    public MensajeContacto(String nombre, String correo, String mensaje) {
        this.nombre = nombre;
        this.correo = correo;
        this.mensaje = mensaje;
        this.creadoEn = Instant.now();
    }

    public void marcarLectura(boolean leido) {
        if (!leido) leidoEn = null;
        else if (leidoEn == null) leidoEn = Instant.now();
    }
}
