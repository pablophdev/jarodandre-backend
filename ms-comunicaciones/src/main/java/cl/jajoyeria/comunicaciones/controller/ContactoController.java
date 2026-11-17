package cl.jajoyeria.comunicaciones.controller;

import cl.jajoyeria.comunicaciones.dto.*;
import cl.jajoyeria.comunicaciones.service.MensajeService;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Contacto Público", description = "Formulario de contacto para consultas de visitantes y clientes")
public class ContactoController {
    private final MensajeService mensajes;

    @Operation(summary = "Enviar mensaje de contacto", description = "Recibe un mensaje de consulta desde la web. Se valida el formato de nombre, correo y contenido (1 a 2000 caracteres).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mensaje recibido correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    @PostMapping("/v1/contacto")
    @ResponseStatus(HttpStatus.CREATED)
    public void recibir(@Valid @RequestBody MensajeRequest datos) {
        mensajes.recibir(datos);
    }

    @Operation(summary = "Bandeja de mensajes de contacto", description = "Lista paginada de mensajes ordenados cronológicamente desde el más reciente. Permite búsqueda por texto y filtro de lectura.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de mensajes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN activo)")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Mensajes", description = "Gestión administrativa de la bandeja de mensajes de contacto")
    @GetMapping("/v1/admin/mensajes")
    public Page<MensajeVista> bandeja(
            @Parameter(description = "Término de búsqueda (busca en nombre, correo y contenido)") @RequestParam(defaultValue="") @Size(max=2000) String q,
            @Parameter(description = "Filtro de lectura: 'todos', 'leidos' o 'sin-leer'") @RequestParam(defaultValue="todos") @Pattern(regexp="todos|leidos|sin-leer") String lectura,
            @Parameter(description = "Número de página (iniciando en 0)") @RequestParam(defaultValue="0") @Min(0) int pagina,
            @Parameter(description = "Cantidad de elementos por página (de 1 a 100)") @RequestParam(defaultValue="25") @Min(1) @Max(100) int tamano) {
        return mensajes.bandeja(q, lectura, pagina, tamano);
    }

    @Operation(summary = "Resumen de mensajes", description = "Obtiene los totales de mensajes acumulados y cantidad de mensajes pendientes de lectura.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumen obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Mensajes")
    @GetMapping("/v1/admin/mensajes/resumen")
    public ResumenMensajes resumen() {
        return mensajes.resumen();
    }

    @Operation(summary = "Actualizar estado de lectura", description = "Marca un mensaje de contacto como leído o no leído.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de lectura actualizado"),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Mensajes")
    @PatchMapping("/v1/admin/mensajes/{id}/lectura")
    public MensajeVista lectura(
            @Parameter(description = "Identificador del mensaje", example = "1") @PathVariable long id,
            @RequestBody @Valid LecturaRequest datos) {
        return mensajes.marcar(id, datos.leido());
    }

    @Operation(summary = "Eliminar mensaje", description = "Elimina permanentemente un mensaje de contacto de la bandeja.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mensaje eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Mensaje no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @Tag(name = "Administración - Mensajes")
    @DeleteMapping("/v1/admin/mensajes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(
            @Parameter(description = "Identificador del mensaje", example = "1") @PathVariable long id) {
        mensajes.eliminar(id);
    }
}
