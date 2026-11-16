package cl.jajoyeria.pedidos.controller;

import cl.jajoyeria.pedidos.dto.*;
import cl.jajoyeria.pedidos.entity.EstadoPago;
import cl.jajoyeria.pedidos.service.*;
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
public class PedidoController {
    private final PedidoService pedidos;
    private final IdentidadService identidad;

    @PostMapping("/v1/checkout")
    public CheckoutVista checkout(@AuthenticationPrincipal Jwt jwt, @RequestHeader(value="X-Carrito-Token", required=false) String token,
            @RequestHeader("Idempotency-Key") UUID clave, @Valid @RequestBody CheckoutRequest datos) {
        return pedidos.checkout(identidad.consultar(jwt), token, clave, datos);
    }

    @PostMapping("/v1/pedidos/{id}/pago-simulado")
    public PedidoVista pagar(@PathVariable long id, @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value="X-Carrito-Token", required=false) String token, @Valid @RequestBody PagoRequest datos) {
        return pedidos.pagar(id, identidad.consultar(jwt), token, datos.accion());
    }

    @GetMapping("/v1/pedidos/{id}")
    public PedidoVista ver(@PathVariable long id, @AuthenticationPrincipal Jwt jwt, @RequestHeader(value="X-Pedido-Token", required=false) String token) {
        return pedidos.consultar(id, identidad.consultar(jwt), token);
    }

    @GetMapping("/v1/pedidos/{id}/comprobante")
    public Comprobante comprobante(@PathVariable long id, @AuthenticationPrincipal Jwt jwt, @RequestHeader(value="X-Pedido-Token", required=false) String token) {
        PedidoVista p = pedidos.consultar(id, identidad.consultar(jwt), token);
        if (p.estadoPago() != EstadoPago.PAGADO) throw new ResponseStatusException(HttpStatus.CONFLICT, "Se requiere pago aprobado");
        return new Comprobante("BOL-%04d".formatted(id), p);
    }

    @PostMapping("/v1/pedidos/{id}/vincular")
    public PedidoVista vincular(@PathVariable long id, @AuthenticationPrincipal Jwt jwt, @RequestHeader("X-Pedido-Token") String token) {
        return pedidos.vincular(id, identidad.consultar(jwt), token);
    }

    @GetMapping("/v1/mis-pedidos")
    public Page<PedidoResumen> historial(@AuthenticationPrincipal Jwt jwt,
            @PageableDefault(sort="id", direction=Sort.Direction.DESC) Pageable pagina) {
        return pedidos.listar(identidad.consultar(jwt).id(), pagina);
    }
}
