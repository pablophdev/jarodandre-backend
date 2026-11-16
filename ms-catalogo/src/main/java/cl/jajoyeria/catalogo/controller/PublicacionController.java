package cl.jajoyeria.catalogo.controller;

import cl.jajoyeria.catalogo.dto.*;
import cl.jajoyeria.catalogo.service.PublicacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Blog Editorial", description = "Lectura pública y gestión administrativa de artículos editoriales de la joyería")
public class PublicacionController {
    private final PublicacionService publicaciones;

    @Operation(summary = "Listar artículos publicados", description = "Retorna todos los artículos del blog en estado PUBLICADO, ordenados cronológicamente desde el más reciente.")
    @ApiResponse(responseCode = "200", description = "Lista de publicaciones obtenida exitosamente")
    @GetMapping("/v1/publicaciones")
    public List<PublicacionResponse> listar() {
        return publicaciones.listar(false);
    }

    @Operation(summary = "Leer artículo por slug", description = "Obtiene un artículo publicado a través de su identificador amigable (slug). Retorna 404 si es un borrador o no existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Artículo encontrado"),
            @ApiResponse(responseCode = "404", description = "Artículo no encontrado o en estado borrador")
    })
    @GetMapping("/v1/publicaciones/{slug}")
    public PublicacionResponse detalle(
            @Parameter(description = "Slug del artículo (ej. 'guia-cuidado-plata-925')", example = "guia-cuidado-plata-925")
            @PathVariable String slug) {
        return publicaciones.porSlug(slug);
    }

    @Operation(summary = "Listar artículos (Administración)", description = "Obtiene todos los artículos (tanto BORRADOR como PUBLICADO) para gestión editorial.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/v1/admin/publicaciones")
    public List<PublicacionResponse> administracion() {
        return publicaciones.listar(true);
    }

    @Operation(summary = "Crear publicación", description = "Crea un nuevo artículo en estado BORRADOR o PUBLICADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Publicación creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (ej. slug duplicado o falta contenido al publicar)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/v1/admin/publicaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicacionResponse crear(@Valid @RequestBody PublicacionRequest datos) {
        return publicaciones.guardar(null, datos);
    }

    @Operation(summary = "Editar publicación", description = "Modifica los datos de una publicación existente, cambia su estado a PUBLICADO o la devuelve a BORRADOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicación actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o slug duplicado"),
            @ApiResponse(responseCode = "404", description = "Publicación no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/v1/admin/publicaciones/{id}")
    public PublicacionResponse editar(
            @Parameter(description = "Identificador de la publicación", example = "1")
            @PathVariable long id,
            @Valid @RequestBody PublicacionRequest datos) {
        return publicaciones.guardar(id, datos);
    }
}
