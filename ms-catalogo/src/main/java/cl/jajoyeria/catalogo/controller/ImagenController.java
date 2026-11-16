package cl.jajoyeria.catalogo.controller;

import cl.jajoyeria.catalogo.dto.ImagenResponse;
import cl.jajoyeria.catalogo.service.ImagenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Tag(name = "Multimedia", description = "Carga y visualización de recursos multimedia de joyas y publicaciones")
public class ImagenController {
    private final ImagenService imagenes;

    @Operation(summary = "Subir imagen", description = "Sube una imagen mediante multipart/form-data. Se valida la firma binaria real (JPG, PNG o WebP) y tamaño máximo de 10 MB. Requiere rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imagen subida exitosamente"),
            @ApiResponse(responseCode = "400", description = "Archivo vacío o excede los 10 MB"),
            @ApiResponse(responseCode = "415", description = "Formato no soportado (se requiere JPG, PNG o WebP)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping(value = "/v1/admin/imagenes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImagenResponse subir(
            @Parameter(description = "Archivo binario de imagen (JPG, PNG o WebP)", required = true)
            @RequestParam MultipartFile imagen) throws IOException {
        return imagenes.guardar(imagen);
    }

    @Operation(summary = "Obtener imagen subida", description = "Retorna el archivo binario de una imagen almacenada en la carpeta uploads/.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Imagen entregada exitosamente",
                    content = @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Imagen no encontrada o formato de nombre inválido")
    })
    @GetMapping("/media/uploads/{nombre}")
    public ResponseEntity<Resource> leer(
            @Parameter(description = "Nombre del archivo de imagen con extensión (UUID.ext)", example = "00000000-0000-0000-0000-000000000001.jpg")
            @PathVariable String nombre) {
        Resource recurso = imagenes.leer(nombre);
        MediaType tipo = MediaTypeFactory.getMediaType(nombre).orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(tipo).body(recurso);
    }
}
