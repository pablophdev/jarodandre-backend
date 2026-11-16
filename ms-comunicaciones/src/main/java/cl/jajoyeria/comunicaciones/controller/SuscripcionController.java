package cl.jajoyeria.comunicaciones.controller;

import cl.jajoyeria.comunicaciones.dto.*;
import cl.jajoyeria.comunicaciones.service.SuscripcionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SuscripcionController {
    private final SuscripcionService suscripciones;

    @PostMapping("/v1/suscripciones")
    public ResponseEntity<AltaRespuesta> alta(@RequestBody @Valid SuscripcionRequest datos) {
        var alta = suscripciones.alta(datos.correo());
        var status = alta.estado() == AltaRespuesta.Estado.ALTA ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(alta);
    }

    @GetMapping("/v1/admin/suscriptores")
    public Page<SuscriptorVista> listar(@RequestParam(defaultValue="") @Size(max=254) String q,
            @RequestParam(defaultValue="0") @Min(0) int pagina, @RequestParam(defaultValue="25") @Min(1) @Max(100) int tamano) {
        return suscripciones.activas(q, pagina, tamano);
    }

    @DeleteMapping("/v1/admin/suscriptores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void baja(@PathVariable long id) {
        suscripciones.baja(id);
    }
}
