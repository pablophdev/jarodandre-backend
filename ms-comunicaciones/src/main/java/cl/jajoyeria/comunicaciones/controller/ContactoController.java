package cl.jajoyeria.comunicaciones.controller;

import cl.jajoyeria.comunicaciones.dto.*;
import cl.jajoyeria.comunicaciones.service.MensajeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ContactoController {
    private final MensajeService mensajes;

    @PostMapping("/v1/contacto")
    @ResponseStatus(HttpStatus.CREATED)
    public void recibir(@Valid @RequestBody MensajeRequest datos) {
        mensajes.recibir(datos);
    }

    @GetMapping("/v1/admin/mensajes")
    public Page<MensajeVista> bandeja(@RequestParam(defaultValue="") @Size(max=2000) String q,
            @RequestParam(defaultValue="todos") @Pattern(regexp="todos|leidos|sin-leer") String lectura,
            @RequestParam(defaultValue="0") @Min(0) int pagina, @RequestParam(defaultValue="25") @Min(1) @Max(100) int tamano) {
        return mensajes.bandeja(q, lectura, pagina, tamano);
    }

    @GetMapping("/v1/admin/mensajes/resumen")
    public ResumenMensajes resumen() {
        return mensajes.resumen();
    }

    @PatchMapping("/v1/admin/mensajes/{id}/lectura")
    public MensajeVista lectura(@PathVariable long id, @RequestBody @Valid LecturaRequest datos) {
        return mensajes.marcar(id, datos.leido());
    }

    @DeleteMapping("/v1/admin/mensajes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable long id) {
        mensajes.eliminar(id);
    }
}
