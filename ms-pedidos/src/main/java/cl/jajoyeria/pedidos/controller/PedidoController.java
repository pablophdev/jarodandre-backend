package cl.jajoyeria.pedidos.controller;

import cl.jajoyeria.pedidos.dto.*;
import cl.jajoyeria.pedidos.entity.EstadoPago;
import cl.jajoyeria.pedidos.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Checkout, pagos y consultas de pedidos")
public class PedidoController {
    private final PedidoService pedidos;
    private final IdentidadService identidad;

    @Operation(summary = "Crear pedido mediante checkout", description = "Revalida precios y disponibilidad del carrito. Idempotency-Key debe ser un UUID; repetir la misma clave y los mismos datos devuelve el mismo resultado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido pendiente creado o recuperado"),
            @ApiResponse(responseCode = "400", description = "Clave o datos de entrega inválidos"),
            @ApiResponse(responseCode = "401", description = "Se requiere una sesión de carrito válida"),
            @ApiResponse(responseCode = "409", description = "El precio cambió, hay un checkout pendiente o los productos no están disponibles"),
            @ApiResponse(responseCode = "503", description = "No hay una pasarela de pago habilitada")
    })
    @PostMapping("/v1/checkout")
    public CheckoutVista checkout(@AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token de sesión de carrito invitado") @RequestHeader(value="X-Carrito-Token", required=false) String token,
            @Parameter(description = "UUID para hacer idempotente el checkout", example = "9a7b3b12-7b6e-4e8d-a5ef-82acb51cd214") @RequestHeader("Idempotency-Key") UUID clave,
            @Valid @RequestBody CheckoutRequest datos) {
        return pedidos.checkout(identidad.consultar(jwt), token, clave, datos);
    }

    @Operation(summary = "Simular resultado del pago", description = "Confirma o cancela el intento de pago simulado del pedido. Al confirmar, vacía el carrito.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago procesado o resultado previo recuperado"),
            @ApiResponse(responseCode = "400", description = "Acción inválida"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado para la sesión de carrito o simulador de pago deshabilitado"),
            @ApiResponse(responseCode = "409", description = "El intento de pago ya fue resuelto")
    })
    @PostMapping("/v1/pedidos/{id}/pago-simulado")
    public PedidoVista pagar(
            @Parameter(description = "Identificador del pedido", example = "1") @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token de sesión de carrito invitado") @RequestHeader(value="X-Carrito-Token", required=false) String token,
            @Valid @RequestBody PagoRequest datos) {
        return pedidos.pagar(id, identidad.consultar(jwt), token, datos.accion());
    }

    @Operation(summary = "Consultar pedido", description = "Devuelve el pedido a su titular, a un administrador o a quien presente el token privado de consulta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido consultado"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado o token de consulta inválido")
    })
    @GetMapping("/v1/pedidos/{id}")
    public PedidoVista ver(
            @Parameter(description = "Identificador del pedido", example = "1") @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token privado de consulta del pedido") @RequestHeader(value="X-Pedido-Token", required=false) String token) {
        return pedidos.consultar(id, identidad.consultar(jwt), token);
    }

    @Operation(summary = "Consultar comprobante de compra", description = "Devuelve los datos imprimibles del comprobante BOL para un pedido con pago aprobado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comprobante obtenido"),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado o token de consulta inválido"),
            @ApiResponse(responseCode = "409", description = "El pedido aún no tiene el pago aprobado")
    })
    @GetMapping("/v1/pedidos/{id}/comprobante")
    public Comprobante comprobante(
            @Parameter(description = "Identificador del pedido", example = "1") @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token privado de consulta del pedido") @RequestHeader(value="X-Pedido-Token", required=false) String token) {
        PedidoVista p = pedidos.consultar(id, identidad.consultar(jwt), token);
        if (p.estadoPago() != EstadoPago.PAGADO) throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere pago aprobado");
        return new Comprobante("BOL-%04d".formatted(id), p);
    }

    @Operation(summary = "Vincular pedido de invitado", description = "Asocia a la cuenta autenticada un pedido de invitado ya resuelto cuyo correo verificado coincide con el de la compra. Revoca el token de consulta del invitado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido vinculado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "El correo de la cuenta no coincide con el de la compra"),
            @ApiResponse(responseCode = "404", description = "Pedido o token de consulta no encontrado"),
            @ApiResponse(responseCode = "409", description = "El pago aún está pendiente")
    })
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/v1/pedidos/{id}/vincular")
    public PedidoVista vincular(
            @Parameter(description = "Identificador del pedido", example = "1") @PathVariable long id,
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Token privado de consulta del pedido") @RequestHeader("X-Pedido-Token") String token) {
        return pedidos.vincular(id, identidad.consultar(jwt), token);
    }

    @Operation(summary = "Historial de pedidos", description = "Lista paginada de pedidos de la cuenta autenticada, ordenados desde el más reciente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial obtenido"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/v1/mis-pedidos")
    public Page<PedidoResumen> historial(@AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(sort="id", direction=Sort.Direction.DESC) Pageable pagina) {
        return pedidos.listar(identidad.consultar(jwt).id(), pagina);
    }
}
