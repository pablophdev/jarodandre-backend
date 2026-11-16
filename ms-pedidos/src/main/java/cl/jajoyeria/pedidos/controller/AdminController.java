package cl.jajoyeria.pedidos.controller;

import cl.jajoyeria.pedidos.dto.*;
import cl.jajoyeria.pedidos.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final IdentidadService identidad;
    private final PedidoService pedidos;
    private final ReporteService reportes;

    @GetMapping("/pedidos")
    public Page<PedidoResumen> listar(@AuthenticationPrincipal Jwt jwt, @PageableDefault(sort="id", direction=Sort.Direction.DESC) Pageable pagina) {
        identidad.exigirAdmin(jwt);
        return pedidos.listar(null, pagina);
    }

    @PatchMapping("/pedidos/{id}/entrega")
    public PedidoVista despachar(@PathVariable long id, @Valid @RequestBody EntregaRequest datos, @AuthenticationPrincipal Jwt jwt) {
        identidad.exigirAdmin(jwt);
        return pedidos.despachar(id, datos.estado());
    }

    @GetMapping("/reportes/ventas")
    public ReporteVentas ventas(@AuthenticationPrincipal Jwt jwt) {
        identidad.exigirAdmin(jwt);
        return reportes.ventas();
    }
}
