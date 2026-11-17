package cl.jajoyeria.comunicaciones.controller;

import cl.jajoyeria.comunicaciones.dto.*;
import cl.jajoyeria.comunicaciones.service.SuscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Suscripciones Públicas", description = "Alta de suscriptores al newsletter de novedades y promociones")
public class SuscripcionController {
    private final SuscripcionService suscripciones;

    @Operation(summary = "Suscribirse al newsletter", description = "Registra un correo electrónico para recibir comunicaciones. Maneja concurrencia de forma atómica: retorna 201 (ALTA) para nuevos registros, 200 (YA_ACTIVA) si ya estaba suscrito, o 200 (REACTIVADA) si había sido dado de baja.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Suscripción registrada exitosamente (estado: ALTA)"),
            @ApiResponse(responseCode = "200", description = "Suscripción ya existente (YA_ACTIVA) o reactivada (REACTIVADA)"),
            @ApiResponse(responseCode = "400", description = "Formato de correo electrónico inválido")
    })
    @PostMapping("/v1/suscripciones")
    public ResponseEntity<AltaRespuesta> alta(@RequestBody @Valid SuscripcionRequest datos) {
        var alta = suscripciones.alta(datos.correo());
        var status = alta.estado() == AltaRespuesta.Estado.ALTA ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(alta);
    }

    @Operation(summary = "Listar suscriptores activos", description = "Obtiene la lista paginada de suscriptores activos en orden descendente por fecha de alta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de suscriptores obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN activo)")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Suscriptores", description = "Gestión administrativa de suscriptores al newsletter")
    @GetMapping("/v1/admin/suscriptores")
    public Page<SuscriptorVista> listar(
            @Parameter(description = "Filtro de búsqueda por coincidencia en el correo electrónico") @RequestParam(defaultValue="") @Size(max=254) String q,
            @Parameter(description = "Número de página (iniciando en 0)") @RequestParam(defaultValue="0") @Min(0) int pagina,
            @Parameter(description = "Cantidad de elementos por página (de 1 a 100)") @RequestParam(defaultValue="25") @Min(1) @Max(100) int tamano) {
        return suscripciones.activas(q, pagina, tamano);
    }

    @Operation(summary = "Dar de baja suscriptor", description = "Registra la fecha de baja del suscriptor y lo retira de las listas activas de difusión.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Suscripción dada de baja exitosamente"),
            @ApiResponse(responseCode = "404", description = "Suscripción no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Suscriptores")
    @DeleteMapping("/v1/admin/suscriptores/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void baja(
            @Parameter(description = "Identificador de la suscripción a dar de baja", example = "10") @PathVariable long id) {
        suscripciones.baja(id);
    }
}
