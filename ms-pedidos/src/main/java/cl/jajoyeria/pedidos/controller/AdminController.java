package cl.jajoyeria.pedidos.controller;

import cl.jajoyeria.pedidos.dto.*;
import cl.jajoyeria.pedidos.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Administración - Pedidos", description = "Gestión administrativa de pedidos, entregas y reportes")
public class AdminController {
    private final IdentidadService identidad;
    private final PedidoService pedidos;
    private final ReporteService reportes;

    @Operation(summary = "Listar pedidos", description = "Obtiene la página de pedidos más recientes para administración.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de pedidos obtenida"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN activo)")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/pedidos")
    public Page<PedidoResumen> listar(@AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(sort="id", direction=Sort.Direction.DESC) Pageable pagina) {
        identidad.exigirAdmin(jwt);
        return pedidos.listar(null, pagina);
    }

    @Operation(summary = "Actualizar estado de entrega", description = "Avanza secuencialmente la entrega de un pedido pagado: ENVIADO y luego COMPLETADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado de entrega actualizado"),
            @ApiResponse(responseCode = "400", description = "Estado inválido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN activo)"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado"),
            @ApiResponse(responseCode = "409", description = "Se requiere pago aprobado y avance secuencial del despacho")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PatchMapping("/pedidos/{id}/entrega")
    public PedidoVista despachar(
            @Parameter(description = "Identificador del pedido", example = "1") @PathVariable long id,
            @Valid @RequestBody EntregaRequest datos, @AuthenticationPrincipal Jwt jwt) {
        identidad.exigirAdmin(jwt);
        return pedidos.despachar(id, datos.estado());
    }

    @Operation(summary = "Reporte de ventas", description = "Obtiene ingresos pagados, ticket promedio, cantidades por estado, productos más vendidos y clientes principales.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (se requiere rol ADMIN activo)")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/reportes/ventas")
    public ReporteVentas ventas(@AuthenticationPrincipal Jwt jwt) {
        identidad.exigirAdmin(jwt);
        return reportes.ventas();
    }
}
