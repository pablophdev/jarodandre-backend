package cl.jajoyeria.catalogo.controller;

import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.service.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PublicacionController {
    private final PublicacionService publicaciones;

    @GetMapping("/v1/publicaciones")
    public List<PublicacionResponse> listar() {
        return publicaciones.listar(false);
    }

    @GetMapping("/v1/publicaciones/{slug}")
    public PublicacionResponse detalle(@PathVariable String slug) {
        return publicaciones.porSlug(slug);
    }

    @GetMapping("/v1/admin/publicaciones")
    public List<PublicacionResponse> administracion() {
        return publicaciones.listar(true);
    }

    @PostMapping("/v1/admin/publicaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicacionResponse crear(@Valid @RequestBody PublicacionRequest datos) {
        return publicaciones.guardar(null, datos);
    }

    @PutMapping("/v1/admin/publicaciones/{id}")
    public PublicacionResponse editar(@PathVariable long id, @Valid @RequestBody PublicacionRequest datos) {
        return publicaciones.guardar(id, datos);
    }
}
