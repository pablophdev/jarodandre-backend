package cl.jajoyeria.catalogo.controller;

import cl.jajoyeria.catalogo.dto.ImagenResponse;
import cl.jajoyeria.catalogo.service.ImagenService;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImagenController {
    private final ImagenService imagenes;

    @PostMapping(value = "/v1/admin/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImagenResponse subir(@RequestParam MultipartFile imagen) throws IOException {
        return imagenes.guardar(imagen);
    }

    @GetMapping("/media/uploads/{nombre}")
    public ResponseEntity<Resource> leer(@PathVariable String nombre) {
        Resource recurso = imagenes.leer(nombre);
        MediaType tipo = MediaTypeFactory.getMediaType(nombre).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(tipo).body(recurso);
    }
}
